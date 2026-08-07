import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Card,
  CardContent,
  CircularProgress,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import MainLayout from '../layouts/MainLayout';
import LectureCalendar from '../components/LectureCalendar';
import { lectureService } from '../services/lectureService';
import { formatTimeOfDay, monthRange } from '../utils/formatters';

/** The current month as a "YYYY-MM" string. */
const currentMonth = () => {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
};

/**
 * Admin view of every teacher's classes on one month grid, with a teacher
 * filter. The filter options are derived from the loaded month's data —
 * whoever actually has classes — rather than a separate users request.
 */
const LectureCalendarAdminPage = () => {
  const [month, setMonth] = useState(currentMonth);
  const [lectures, setLectures] = useState([]);
  const [teacherId, setTeacherId] = useState('all');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadMonth = useCallback(async (value) => {
    const range = monthRange(value);
    if (!range) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response = await lectureService.getCalendar(range.from, range.to);
      setLectures(response.data ?? []);
    } catch (err) {
      setError(err?.message ?? 'Failed to load the calendar.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadMonth(month);
  }, [month, loadMonth]);

  /** Distinct teachers present in this month, sorted by name. */
  const teachers = useMemo(() => {
    const map = new Map();
    for (const lecture of lectures) {
      map.set(lecture.teacherId, lecture.teacherFullName);
    }
    return [...map.entries()]
      .map(([id, name]) => ({ id, name }))
      .sort((a, b) => a.name.localeCompare(b.name));
  }, [lectures]);

  const visibleLectures = useMemo(
    () => (teacherId === 'all'
      ? lectures
      : lectures.filter((lecture) => lecture.teacherId === teacherId)),
    [lectures, teacherId],
  );

  return (
    <MainLayout>
      <Stack spacing={3}>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          justifyContent="space-between"
          alignItems={{ xs: 'stretch', sm: 'center' }}
          spacing={2}
        >
          <Box>
            <Typography variant="h4" component="h1" fontWeight={600}>
              Lecture Calendar
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Every teacher&apos;s classes for the month — click a day for details.
            </Typography>
          </Box>
          <TextField
            select
            label="Teacher"
            value={teacherId}
            onChange={(event) => setTeacherId(event.target.value)}
            size="small"
            sx={{ minWidth: 220 }}
          >
            <MenuItem value="all">All teachers</MenuItem>
            {teachers.map((teacher) => (
              <MenuItem key={teacher.id} value={teacher.id}>
                {teacher.name}
              </MenuItem>
            ))}
          </TextField>
        </Stack>

        {error && (
          <Alert severity="error" onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        <Card elevation={2}>
          <CardContent>
            {loading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
                <CircularProgress />
              </Box>
            ) : (
              <LectureCalendar
                month={month}
                lectures={visibleLectures}
                onMonthChange={(next) => {
                  // A new month may hold different teachers; reset the filter
                  // so the calendar never looks silently empty.
                  setTeacherId('all');
                  setMonth(next);
                }}
                renderEntry={(lecture) =>
                  `${formatTimeOfDay(lecture.startTime)} ${lecture.teacherFullName?.split(' ')[0] ?? ''} · ${lecture.subject}`}
              />
            )}
          </CardContent>
        </Card>
      </Stack>
    </MainLayout>
  );
};

export default LectureCalendarAdminPage;
