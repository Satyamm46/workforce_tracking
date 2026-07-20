import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
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
  Button,
} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import MainLayout from '../layouts/MainLayout';
import { workReportService } from '../services/workReportService';
import { formatDateTime, formatTime, formatTimeOfDay } from '../utils/formatters';
import { downloadCsv } from '../utils/csv';

const PAGE_SIZE = 20;
/** Page size used when gathering every row of a day for CSV export. */
const EXPORT_PAGE_SIZE = 100;

/** Today's date as the YYYY-MM-DD string the date input expects. */
const todayISO = () => new Date().toISOString().slice(0, 10);

/**
 * Manager view of all submitted work reports for a selected day.
 */
const WorkReportsAdminPage = () => {
  const [date, setDate] = useState(todayISO());
  const [pageData, setPageData] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [exporting, setExporting] = useState(false);

  const loadReports = useCallback(async (selectedDate, pageNumber) => {
    setLoading(true);
    setError(null);
    try {
      const response = await workReportService.getReportsByDate(selectedDate, pageNumber, PAGE_SIZE);
      setPageData(response.data);
    } catch (err) {
      setError(err?.message ?? 'Failed to load reports.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadReports(date, page);
  }, [date, page, loadReports]);

  /**
   * Exports every report for the selected day — not just the visible page.
   * Walks all pages (the list view is paginated) so the CSV is complete.
   */
  const handleExport = async () => {
    setError(null);
    setExporting(true);
    try {
      const rows = [];
      let pageNumber = 0;
      let last = false;
      do {
        const response = await workReportService.getReportsByDate(
          date, pageNumber, EXPORT_PAGE_SIZE);
        const data = response.data;
        rows.push(...data.content);
        last = data.last;
        pageNumber += 1;
      } while (!last);

      if (rows.length === 0) {
        setError('No reports to export for this day.');
        return;
      }

      const schedule = (report) =>
        report.plannedStartTime && report.plannedEndTime
          ? `${formatTimeOfDay(report.plannedStartTime)}–${formatTimeOfDay(report.plannedEndTime)}`
          : '—';

      downloadCsv(
        `work-reports-${date}.csv`,
        ['Name', 'Work Date', 'Check-in Time', 'Checkout Time', 'Work Schedule',
          'Report', 'Submitted At', 'Status'],
        rows.map((report) => [
          report.userFullName,
          report.workDate,
          formatTime(report.checkInTime),
          formatTime(report.checkoutTime),
          schedule(report),
          report.reportText,
          formatDateTime(report.submittedAt),
          report.submittedLate ? 'Late' : 'On time',
        ])
      );
    } catch (err) {
      setError(err?.message ?? 'Failed to export reports.');
    } finally {
      setExporting(false);
    }
  };

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
              Work Reports
            </Typography>
            <Typography variant="body1" color="text.secondary">
              End-of-day reports submitted by staff for the selected day.
            </Typography>
          </Box>
          <Stack direction="row" spacing={2} alignItems="center">
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
            <Button
              variant="outlined"
              startIcon={<DownloadIcon />}
              onClick={handleExport}
              disabled={exporting || loading}
            >
              {exporting ? 'Exporting…' : 'Export CSV'}
            </Button>
          </Stack>
        </Stack>

        {error && (
          <Alert severity="error" onClose={() => setError(null)}>
            {error}
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
                No reports submitted for this day.
              </Typography>
            </Box>
          ) : (
            <>
              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Name</TableCell>
                      <TableCell>Work Date</TableCell>
                      <TableCell>Report</TableCell>
                      <TableCell>Submitted At</TableCell>
                      <TableCell>Status</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {pageData.content.map((report) => (
                      <TableRow key={report.id} hover>
                        <TableCell>{report.userFullName}</TableCell>
                        <TableCell>{report.workDate}</TableCell>
                        <TableCell sx={{ maxWidth: 450 }}>
                          <Typography variant="body2" noWrap title={report.reportText}>
                            {report.reportText}
                          </Typography>
                        </TableCell>
                        <TableCell>{formatDateTime(report.submittedAt)}</TableCell>
                        <TableCell>
                          <Chip
                            label={report.submittedLate ? 'Late' : 'On time'}
                            color={report.submittedLate ? 'warning' : 'success'}
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

export default WorkReportsAdminPage;
