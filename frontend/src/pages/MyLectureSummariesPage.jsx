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
  Typography,
} from '@mui/material';
import MainLayout from '../layouts/MainLayout';
import { lectureSummaryService } from '../services/lectureSummaryService';
import { formatDateTime } from '../utils/formatters';

const PAGE_SIZE = 10;

/**
 * Teacher's lecture summary history, and a button to submit a summary for any
 * completed lecture from the history. The "Submit Summary" action is also
 * available on My Lectures — this page is the dedicated history view.
 */
const MyLectureSummariesPage = () => {
  const [summaries, setSummaries] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadSummaries = useCallback(async (pageNumber) => {
    setLoading(true);
    setError(null);
    try {
      const response = await lectureSummaryService.getMySummaries(pageNumber, PAGE_SIZE);
      setSummaries(response.data);
    } catch (err) {
      setError(err?.message ?? 'Failed to load summaries.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadSummaries(page);
  }, [page, loadSummaries]);

  return (
    <MainLayout>
      <Stack spacing={3}>
        <Box>
          <Typography variant="h4" component="h1" fontWeight={600}>
            My Lecture Summaries
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Post-lecture summaries you have submitted. Use My Lectures to submit a summary for a
            completed lecture (within 24 hours of it ending).
          </Typography>
        </Box>

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
          ) : !summaries || summaries.content.length === 0 ? (
            <Box sx={{ p: 6, textAlign: 'center' }}>
              <Typography color="text.secondary">No summaries submitted yet.</Typography>
            </Box>
          ) : (
            <>
              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Date</TableCell>
                      <TableCell>Subject</TableCell>
                      <TableCell>Class</TableCell>
                      <TableCell>Summary</TableCell>
                      <TableCell>Submitted At</TableCell>
                      <TableCell>Status</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {summaries.content.map((s) => (
                      <TableRow key={s.id} hover>
                        <TableCell>{s.lectureDate}</TableCell>
                        <TableCell>{s.subject}</TableCell>
                        <TableCell>{s.className}</TableCell>
                        <TableCell sx={{ maxWidth: 380 }}>
                          <Typography variant="body2" noWrap title={s.summaryText}>
                            {s.summaryText}
                          </Typography>
                        </TableCell>
                        <TableCell>{formatDateTime(s.submittedAt)}</TableCell>
                        <TableCell>
                          <Chip
                            label={s.submittedLate ? 'Late' : 'On time'}
                            color={s.submittedLate ? 'warning' : 'success'}
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
                  Page {summaries.page + 1} of {summaries.totalPages} · {summaries.totalElements} total
                </Typography>
                <Stack direction="row" spacing={1}>
                  <Button size="small" disabled={summaries.first} onClick={() => setPage((p) => p - 1)}>
                    Previous
                  </Button>
                  <Button size="small" disabled={summaries.last} onClick={() => setPage((p) => p + 1)}>
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

export default MyLectureSummariesPage;
