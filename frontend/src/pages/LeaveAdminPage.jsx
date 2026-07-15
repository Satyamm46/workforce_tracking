import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  MenuItem,
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
import CheckIcon from '@mui/icons-material/Check';
import CloseIcon from '@mui/icons-material/Close';
import MainLayout from '../layouts/MainLayout';
import LeaveStatusChip from '../components/LeaveStatusChip';
import { leaveService } from '../services/leaveService';

const PAGE_SIZE = 20;

const STATUS_OPTIONS = ['PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'];

/**
 * Admin screen for reviewing leave requests: a status-filtered queue with
 * approve/reject decisions collected through a comment dialog.
 */
const LeaveAdminPage = () => {
  const [status, setStatus] = useState('PENDING');
  const [pageData, setPageData] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // The pending decision: { leave, action: 'approve' | 'reject' } or null.
  const [decision, setDecision] = useState(null);
  const [comment, setComment] = useState('');
  const [deciding, setDeciding] = useState(false);

  const loadLeaves = useCallback(async (selectedStatus, pageNumber) => {
    setLoading(true);
    setError(null);
    try {
      const response = await leaveService.getLeavesByStatus(
        selectedStatus, pageNumber, PAGE_SIZE);
      setPageData(response.data);
    } catch (err) {
      setError(err?.message ?? 'Failed to load leave requests.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadLeaves(status, page);
  }, [status, page, loadLeaves]);

  const openDecision = (leave, action) => {
    setDecision({ leave, action });
    setComment('');
  };

  const handleDecide = async () => {
    setDeciding(true);
    setError(null);
    try {
      if (decision.action === 'approve') {
        await leaveService.approveLeave(decision.leave.id, comment);
      } else {
        await leaveService.rejectLeave(decision.leave.id, comment);
      }
      setDecision(null);
      await loadLeaves(status, page);
    } catch (err) {
      setDecision(null);
      setError(err?.message ?? 'Decision failed.');
    } finally {
      setDeciding(false);
    }
  };

  return (
    <MainLayout>
      <Stack spacing={3}>
        <Stack direction="row" justifyContent="space-between" alignItems="flex-end">
          <Box>
            <Typography variant="h4" component="h1" fontWeight={600}>
              Leave Requests
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Review and decide employee leave.
            </Typography>
          </Box>
          <TextField
            label="Status"
            select
            size="small"
            value={status}
            onChange={(e) => {
              setStatus(e.target.value);
              setPage(0);
            }}
            sx={{ minWidth: 160 }}
          >
            {STATUS_OPTIONS.map((option) => (
              <MenuItem key={option} value={option}>
                {option}
              </MenuItem>
            ))}
          </TextField>
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
                No {status.toLowerCase()} requests.
              </Typography>
            </Box>
          ) : (
            <>
              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Employee</TableCell>
                      <TableCell>From</TableCell>
                      <TableCell>To</TableCell>
                      <TableCell>Days</TableCell>
                      <TableCell>Reason</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell align="right">Decision</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {pageData.content.map((leave) => (
                      <TableRow key={leave.id} hover>
                        <TableCell>{leave.userFullName}</TableCell>
                        <TableCell>{leave.startDate}</TableCell>
                        <TableCell>{leave.endDate}</TableCell>
                        <TableCell>{leave.totalDays}</TableCell>
                        <TableCell>{leave.reason}</TableCell>
                        <TableCell>
                          <LeaveStatusChip status={leave.status} />
                        </TableCell>
                        <TableCell align="right">
                          {leave.status === 'PENDING' ? (
                            <Stack direction="row" spacing={1} justifyContent="flex-end">
                              <Button
                                size="small"
                                color="success"
                                variant="outlined"
                                startIcon={<CheckIcon />}
                                onClick={() => openDecision(leave, 'approve')}
                              >
                                Approve
                              </Button>
                              <Button
                                size="small"
                                color="error"
                                variant="outlined"
                                startIcon={<CloseIcon />}
                                onClick={() => openDecision(leave, 'reject')}
                              >
                                Reject
                              </Button>
                            </Stack>
                          ) : (
                            <Typography variant="body2" color="text.secondary">
                              {leave.decidedByFullName ?? '—'}
                            </Typography>
                          )}
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
                  Page {pageData.page + 1} of {pageData.totalPages} · {pageData.totalElements}{' '}
                  total
                </Typography>
                <Stack direction="row" spacing={1}>
                  <Button
                    size="small"
                    disabled={pageData.first}
                    onClick={() => setPage((p) => p - 1)}
                  >
                    Previous
                  </Button>
                  <Button
                    size="small"
                    disabled={pageData.last}
                    onClick={() => setPage((p) => p + 1)}
                  >
                    Next
                  </Button>
                </Stack>
              </Stack>
            </>
          )}
        </Paper>
      </Stack>

      {/* ---- Decision dialog ---- */}
      <Dialog open={decision !== null} onClose={() => !deciding && setDecision(null)} fullWidth>
        <DialogTitle>
          {decision?.action === 'approve' ? 'Approve' : 'Reject'} leave request?
        </DialogTitle>
        <DialogContent>
          <DialogContentText sx={{ mb: 2 }}>
            {decision?.leave.userFullName}: {decision?.leave.startDate} to{' '}
            {decision?.leave.endDate} ({decision?.leave.totalDays} day(s)).
            {decision?.action === 'approve' &&
              ' Approval will mark these days as On Leave in attendance.'}
          </DialogContentText>
          <TextField
            label="Comment (optional)"
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            fullWidth
            multiline
            rows={2}
            disabled={deciding}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDecision(null)} disabled={deciding}>
            Cancel
          </Button>
          <Button
            color={decision?.action === 'approve' ? 'success' : 'error'}
            variant="contained"
            onClick={handleDecide}
            disabled={deciding}
          >
            {deciding ? (
              <CircularProgress size={22} color="inherit" />
            ) : decision?.action === 'approve' ? (
              'Approve'
            ) : (
              'Reject'
            )}
          </Button>
        </DialogActions>
      </Dialog>
    </MainLayout>
  );
};

export default LeaveAdminPage;
