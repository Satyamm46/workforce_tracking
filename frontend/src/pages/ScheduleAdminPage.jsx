import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import MainLayout from '../layouts/MainLayout';
import { workPlanService } from '../services/workPlanService';
import { formatTimeOfDay } from '../utils/formatters';

const PAGE_SIZE = 20;

/** Today's date as the YYYY-MM-DD string the date input expects. */
const todayISO = () => new Date().toISOString().slice(0, 10);

/**
 * Manager view of everyone's declared schedules for a day, plus the list of
 * Admins/Employees who have not submitted one.
 */
const ScheduleAdminPage = () => {
  const [date, setDate] = useState(todayISO());
  const [pageData, setPageData] = useState(null);
  const [missing, setMissing] = useState([]);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadPlans = useCallback(async (selectedDate, pageNumber) => {
    setLoading(true);
    setError(null);
    try {
      const [plansResponse, missingResponse] = await Promise.all([
        workPlanService.getPlansByDate(selectedDate, pageNumber, PAGE_SIZE),
        workPlanService.getMissingSubmitters(selectedDate),
      ]);
      setPageData(plansResponse.data);
      setMissing(missingResponse.data);
    } catch (err) {
      setError(err?.message ?? 'Failed to load schedules.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadPlans(date, page);
  }, [date, page, loadPlans]);

  return (
    <MainLayout>
      <Stack spacing={3}>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          justifyContent="space-between"
          alignItems={{ sm: 'center' }}
          spacing={2}
        >
          <Box>
            <Typography variant="h4" component="h1" fontWeight={600}>
              Schedules
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Declared work plans for the selected day.
            </Typography>
          </Box>
          <TextField
            label="Date"
            type="date"
            value={date}
            onChange={(e) => {
              setDate(e.target.value);
              setPage(0);
            }}
            slotProps={{ inputLabel: { shrink: true } }}
          />
        </Stack>

        {error && (
          <Alert severity="error" onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        {missing.length > 0 && (
          <Alert severity="warning">
            Not submitted ({missing.length}): {missing.join(', ')}
          </Alert>
        )}

        <Paper elevation={2}>
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
              <CircularProgress />
            </Box>
          ) : !pageData || pageData.content.length === 0 ? (
            <Box sx={{ p: 6, textAlign: 'center' }}>
              <Typography color="text.secondary">
                No schedules submitted for this day.
              </Typography>
            </Box>
          ) : (
            <>
              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Name</TableCell>
                      <TableCell>Planned Login</TableCell>
                      <TableCell>Planned Logout</TableCell>
                      <TableCell>Planned Work</TableCell>
                      <TableCell>Submitted</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {pageData.content.map((plan) => (
                      <TableRow key={plan.id} hover>
                        <TableCell>{plan.userFullName}</TableCell>
                        <TableCell>{formatTimeOfDay(plan.plannedStartTime)}</TableCell>
                        <TableCell>{formatTimeOfDay(plan.plannedEndTime)}</TableCell>
                        <TableCell sx={{ maxWidth: 420 }}>
                          <Typography variant="body2" noWrap title={plan.workDescription}>
                            {plan.workDescription}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Chip
                            label={plan.submittedLate ? 'Late' : 'On time'}
                            color={plan.submittedLate ? 'warning' : 'success'}
                            size="small"
                          />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>

              <Stack
                direction="row"
                justifyContent="space-between"
                alignItems="center"
                sx={{ p: 2 }}
              >
                <Typography variant="body2" color="text.secondary">
                  Page {pageData.page + 1} of {pageData.totalPages} · {pageData.totalElements} total
                </Typography>
                <Stack direction="row" spacing={1}>
                  <Button size="small" disabled={pageData.first} onClick={() => setPage((p) => p - 1)}>
                    Previous
                  </Button>
                  <Button size="small" disabled={pageData.last} onClick={() => setPage((p) => p + 1)}>
                    Next
                  </Button>
                </Stack>
              </Stack>
            </>
          )}
        </Paper>
      </Stack>
    </MainLayout>
  );
};

export default ScheduleAdminPage;
