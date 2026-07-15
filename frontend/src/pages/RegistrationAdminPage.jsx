import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
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
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import MainLayout from '../layouts/MainLayout';
import { registrationService } from '../services/registrationService';
import { formatDateTime } from '../utils/formatters';

const PAGE_SIZE = 10;

/** Roles the Super Admin may assign on approval. */
const ASSIGNABLE_ROLES = ['EMPLOYEE', 'TEACHER', 'ADMIN'];

const STATUS_COLORS = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'error',
};

/**
 * Super Admin screen for reviewing self-registration requests. Approving
 * creates the user account (with an optional role override); rejecting
 * records the decision with an optional comment.
 */
const RegistrationAdminPage = () => {
  const [status, setStatus] = useState('PENDING');
  const [pageData, setPageData] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // { request, action: 'approve' | 'reject' } — controls the decision dialog.
  const [decision, setDecision] = useState(null);
  const [decisionRole, setDecisionRole] = useState('EMPLOYEE');
  const [decisionComment, setDecisionComment] = useState('');
  const [deciding, setDeciding] = useState(false);

  const loadRequests = useCallback(async (statusFilter, pageNumber) => {
    setLoading(true);
    setError(null);
    try {
      const response = await registrationService.getRequests(
        statusFilter,
        pageNumber,
        PAGE_SIZE
      );
      setPageData(response.data);
    } catch (err) {
      setError(err?.message ?? 'Failed to load registration requests.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadRequests(status, page);
  }, [status, page, loadRequests]);

  const handleStatusChange = (_event, newStatus) => {
    if (newStatus !== null) {
      setStatus(newStatus);
      setPage(0);
    }
  };

  const openDecision = (request, action) => {
    setDecision({ request, action });
    setDecisionRole(request.requestedRole);
    setDecisionComment('');
  };

  const closeDecision = () => {
    if (!deciding) {
      setDecision(null);
    }
  };

  const submitDecision = async () => {
    setDeciding(true);
    setError(null);
    try {
      if (decision.action === 'approve') {
        await registrationService.approve(decision.request.id, {
          role: decisionRole,
          comment: decisionComment || null,
        });
      } else {
        await registrationService.reject(decision.request.id, {
          comment: decisionComment || null,
        });
      }
      setDecision(null);
      await loadRequests(status, page);
    } catch (err) {
      setError(err?.message ?? 'Decision failed.');
      setDecision(null);
    } finally {
      setDeciding(false);
    }
  };

  return (
    <MainLayout>
      <Stack spacing={3}>
        <Stack direction="row" justifyContent="space-between" alignItems="center">
          <Box>
            <Typography variant="h4" component="h1" fontWeight={600}>
              Registrations
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Approve or reject account requests.
            </Typography>
          </Box>
          <ToggleButtonGroup
            value={status}
            exclusive
            onChange={handleStatusChange}
            size="small"
          >
            <ToggleButton value="PENDING">Pending</ToggleButton>
            <ToggleButton value="APPROVED">Approved</ToggleButton>
            <ToggleButton value="REJECTED">Rejected</ToggleButton>
          </ToggleButtonGroup>
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
                No {status.toLowerCase()} registration requests.
              </Typography>
            </Box>
          ) : (
            <>
              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Name</TableCell>
                      <TableCell>Email</TableCell>
                      <TableCell>Requested Role</TableCell>
                      <TableCell>Submitted</TableCell>
                      <TableCell>Status</TableCell>
                      {status !== 'PENDING' && <TableCell>Decided By</TableCell>}
                      {status === 'PENDING' && <TableCell align="right">Actions</TableCell>}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {pageData.content.map((request) => (
                      <TableRow key={request.id} hover>
                        <TableCell>{request.fullName}</TableCell>
                        <TableCell>{request.email}</TableCell>
                        <TableCell>
                          <Chip label={request.requestedRole} size="small" />
                        </TableCell>
                        <TableCell>{formatDateTime(request.createdAt)}</TableCell>
                        <TableCell>
                          <Chip
                            label={request.status}
                            color={STATUS_COLORS[request.status] ?? 'default'}
                            size="small"
                          />
                        </TableCell>
                        {status !== 'PENDING' && (
                          <TableCell>{request.decidedByName ?? '—'}</TableCell>
                        )}
                        {status === 'PENDING' && (
                          <TableCell align="right">
                            <Stack direction="row" spacing={1} justifyContent="flex-end">
                              <Button
                                size="small"
                                color="success"
                                variant="outlined"
                                startIcon={<CheckCircleIcon />}
                                onClick={() => openDecision(request, 'approve')}
                              >
                                Approve
                              </Button>
                              <Button
                                size="small"
                                color="error"
                                variant="outlined"
                                startIcon={<CancelIcon />}
                                onClick={() => openDecision(request, 'reject')}
                              >
                                Reject
                              </Button>
                            </Stack>
                          </TableCell>
                        )}
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

      {/* Decision dialog */}
      <Dialog open={decision !== null} onClose={closeDecision} maxWidth="xs" fullWidth>
        <DialogTitle>
          {decision?.action === 'approve' ? 'Approve Registration' : 'Reject Registration'}
        </DialogTitle>
        <DialogContent>
          <Stack spacing={2.5} sx={{ mt: 1 }}>
            <Typography variant="body2">
              {decision?.request.fullName} ({decision?.request.email})
            </Typography>

            {decision?.action === 'approve' && (
              <TextField
                label="Role to assign"
                select
                value={decisionRole}
                onChange={(e) => setDecisionRole(e.target.value)}
                fullWidth
                disabled={deciding}
              >
                {ASSIGNABLE_ROLES.map((role) => (
                  <MenuItem key={role} value={role}>
                    {role}
                  </MenuItem>
                ))}
              </TextField>
            )}

            <TextField
              label="Comment (optional)"
              value={decisionComment}
              onChange={(e) => setDecisionComment(e.target.value)}
              fullWidth
              multiline
              minRows={2}
              disabled={deciding}
            />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={closeDecision} disabled={deciding}>
            Cancel
          </Button>
          <Button
            variant="contained"
            color={decision?.action === 'approve' ? 'success' : 'error'}
            onClick={submitDecision}
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

export default RegistrationAdminPage;
