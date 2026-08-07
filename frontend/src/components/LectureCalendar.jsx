import { useMemo, useState } from 'react';
import {
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  List,
  ListItem,
  ListItemText,
  Stack,
  Typography,
} from '@mui/material';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import AddIcon from '@mui/icons-material/Add';
import LectureStatusChip from './LectureStatusChip';
import { formatTimeOfDay } from '../utils/formatters';

/** Chip colour per lecture status, matching LectureStatusChip's palette. */
const STATUS_COLORS = {
  SCHEDULED: 'info',
  LIVE: 'success',
  COMPLETED: 'default',
  CANCELLED: 'error',
  MISSED: 'warning',
  SUMMARY_MISSED: 'error',
};

const WEEKDAY_HEADERS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

/** "2026-08" -> "August 2026", for the header. */
const monthLabel = (month) => {
  const [year, mon] = month.split('-').map(Number);
  return new Date(year, mon - 1, 1).toLocaleString(undefined, {
    month: 'long',
    year: 'numeric',
  });
};

/** The "YYYY-MM" one step away from the given month; delta is +1 or -1. */
const shiftMonth = (month, delta) => {
  const [year, mon] = month.split('-').map(Number);
  const date = new Date(year, mon - 1 + delta, 1);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
};

/** Today as a "YYYY-MM-DD" string in the browser's zone. */
const todayIso = () => {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
};

/** "YYYY-MM-DD" -> "Thursday, 7 August 2026", for the day dialog title. */
const dayLabel = (date) => {
  const [year, mon, day] = date.split('-').map(Number);
  return new Date(year, mon - 1, day).toLocaleDateString(undefined, {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });
};

/**
 * A month-grid calendar of lectures, hand-built from MUI primitives (the
 * project deliberately carries no date library). Weeks start on Monday.
 *
 * Zoom-style interaction: EVERY day is clickable — empty ones too. Clicking a
 * day opens a dialog listing its classes (if any), and, when `onSchedule` is
 * provided, offers a "Schedule class" action for that date so a teacher can
 * plan straight from the grid. The `renderEntry` prop lets the admin calendar
 * prefix the teacher's name without this component knowing about roles.
 *
 * Props:
 * - month: "YYYY-MM" being displayed
 * - lectures: LectureResponse[] for that month
 * - onMonthChange: (nextMonth: "YYYY-MM") => void
 * - onSchedule?: (date: "YYYY-MM-DD") => void — shows the schedule action
 * - renderEntry?: (lecture) => string — chip label override
 */
const LectureCalendar = ({ month, lectures, onMonthChange, onSchedule, renderEntry }) => {
  const [selectedDay, setSelectedDay] = useState(null); // "YYYY-MM-DD" or null

  const today = todayIso();

  /** lectures grouped by "YYYY-MM-DD"; backend returns them time-sorted. */
  const byDay = useMemo(() => {
    const groups = {};
    for (const lecture of lectures ?? []) {
      (groups[lecture.lectureDate] ??= []).push(lecture);
    }
    return groups;
  }, [lectures]);

  /** The grid: leading nulls to align day 1 under its weekday, then each day. */
  const cells = useMemo(() => {
    const [year, mon] = month.split('-').map(Number);
    const daysInMonth = new Date(year, mon, 0).getDate();
    // getDay(): 0=Sun..6=Sat -> Monday-first offset 0=Mon..6=Sun.
    const firstWeekday = (new Date(year, mon - 1, 1).getDay() + 6) % 7;
    const result = Array.from({ length: firstWeekday }, () => null);
    for (let day = 1; day <= daysInMonth; day++) {
      result.push(`${year}-${String(mon).padStart(2, '0')}-${String(day).padStart(2, '0')}`);
    }
    return result;
  }, [month]);

  const entryLabel = (lecture) =>
    renderEntry
      ? renderEntry(lecture)
      : `${formatTimeOfDay(lecture.startTime)} ${lecture.subject}`;

  const selectedLectures = selectedDay ? (byDay[selectedDay] ?? []) : [];

  return (
    <Box>
      {/* ---- Month navigation ---- */}
      <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 2 }}>
        <IconButton onClick={() => onMonthChange(shiftMonth(month, -1))} aria-label="Previous month">
          <ChevronLeftIcon />
        </IconButton>
        <Typography variant="h6" fontWeight={600}>
          {monthLabel(month)}
        </Typography>
        <IconButton onClick={() => onMonthChange(shiftMonth(month, 1))} aria-label="Next month">
          <ChevronRightIcon />
        </IconButton>
      </Stack>

      {/* ---- Weekday headers ---- */}
      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 0.5, mb: 0.5 }}>
        {WEEKDAY_HEADERS.map((label) => (
          <Typography key={label} variant="caption" color="text.secondary"
            sx={{ textAlign: 'center', fontWeight: 600 }}>
            {label}
          </Typography>
        ))}
      </Box>

      {/* ---- Day grid ---- */}
      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 0.5 }}>
        {cells.map((date, index) => {
          if (!date) {
            return <Box key={`blank-${index}`} />;
          }
          const dayLectures = byDay[date] ?? [];
          const isToday = date === today;
          return (
            <Box
              key={date}
              onClick={() => setSelectedDay(date)}
              sx={{
                minHeight: { xs: 64, sm: 96 },
                p: 0.5,
                borderRadius: 1,
                border: '1px solid',
                borderColor: isToday ? 'primary.main' : 'divider',
                borderWidth: isToday ? 2 : 1,
                bgcolor: 'background.paper',
                cursor: 'pointer',
                overflow: 'hidden',
                '&:hover': { bgcolor: 'action.hover' },
              }}
            >
              <Typography variant="caption" fontWeight={isToday ? 700 : 400}
                color={isToday ? 'primary' : 'text.secondary'}>
                {Number(date.slice(8))}
              </Typography>
              <Stack spacing={0.25} sx={{ mt: 0.25 }}>
                {dayLectures.slice(0, 3).map((lecture) => (
                  <Chip
                    key={lecture.id}
                    label={entryLabel(lecture)}
                    color={STATUS_COLORS[lecture.status] ?? 'default'}
                    size="small"
                    sx={{
                      height: 18,
                      justifyContent: 'flex-start',
                      display: { xs: 'none', sm: 'inline-flex' },
                      '& .MuiChip-label': { px: 0.5, fontSize: '0.65rem' },
                    }}
                  />
                ))}
                {/* On phones the chips are hidden; show a count dot instead. */}
                {dayLectures.length > 0 && (
                  <Typography variant="caption" color="primary"
                    sx={{ display: { xs: 'block', sm: 'none' }, fontWeight: 600 }}>
                    {dayLectures.length}
                  </Typography>
                )}
                {dayLectures.length > 3 && (
                  <Typography variant="caption" color="text.secondary"
                    sx={{ display: { xs: 'none', sm: 'block' } }}>
                    +{dayLectures.length - 3} more
                  </Typography>
                )}
              </Stack>
            </Box>
          );
        })}
      </Box>

      {/* ---- Day detail dialog ---- */}
      <Dialog open={selectedDay !== null} onClose={() => setSelectedDay(null)} maxWidth="sm" fullWidth>
        <DialogTitle>{selectedDay ? dayLabel(selectedDay) : ''}</DialogTitle>
        <DialogContent>
          {selectedLectures.length === 0 ? (
            <Typography color="text.secondary" sx={{ py: 2 }}>
              No classes on this day.
            </Typography>
          ) : (
            <List dense>
              {selectedLectures.map((lecture) => (
                <ListItem key={lecture.id}
                  secondaryAction={<LectureStatusChip status={lecture.status} />}>
                  <ListItemText
                    primary={`${formatTimeOfDay(lecture.startTime)} – ${formatTimeOfDay(lecture.effectiveEndTime ?? lecture.endTime)}  ·  ${lecture.subject}`}
                    secondary={[
                      lecture.teacherFullName,
                      lecture.className,
                      lecture.batch,
                    ].filter(Boolean).join(' · ')}
                  />
                </ListItem>
              ))}
            </List>
          )}
        </DialogContent>
        <DialogActions>
          {/* Zoom-style: schedule straight from the day you clicked. Only for
              days from today onward — the past can't hold a new class. */}
          {onSchedule && selectedDay && selectedDay >= today && (
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => {
                const date = selectedDay;
                setSelectedDay(null);
                onSchedule(date);
              }}
            >
              Schedule class
            </Button>
          )}
          <Button onClick={() => setSelectedDay(null)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default LectureCalendar;
