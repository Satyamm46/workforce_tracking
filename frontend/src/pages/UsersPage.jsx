import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  IconButton,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tooltip,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import MainLayout from '../layouts/MainLayout';
import { userService } from '../services/userService';

const PAGE_SIZE = 10;

/**
 * Admin screen listing users with pagination and per-row status actions.
 * Data and paging metadata come from the backend's PagedResponse.
 */
const UsersPage = () => {
  const navigate = useNavigate();

  const [pageData, setPageData] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionId, setActionId] = useState(null); // id of the row being toggled

  const loadUsers = useCallback(async (pageNumber) => {
    setLoading(true);
    setError(null);
    try {
      const response = await userService.getUsers(pageNumber, PAGE_SIZE);
      setPageData(response.data);
    } catch (err) {
      setError(err?.message ?? 'Failed to load users.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadUsers(page);
  }, [page, loadUsers]);

  const handleToggleStatus = async (user) => {
    setActionId(user.id);
    setError(null);
    try {
      if (user.enabled) {
        await userService.deactivateUser(user.id);
      } else {
        await userService.activateUser(user.id);
      }
      await loadUsers(page);
    } catch (err) {
      setError(err?.message ?? 'Action failed.');
    } finally {
      setActionId(null);
    }
  };

  return (
    <MainLayout>
      <Stack spacing={3}>
        <Stack direction="row" justifyContent="space-between" alignItems="center">
          <Box>
            <Typography variant="h4" component="h1" fontWeight={600}>
              Users
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Manage employees and teachers.
            </Typography>
          </Box>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/users/new')}>
            Add User
          </Button>
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
              <Typography color="text.secondary">No users found.</Typography>
            </Box>
          ) : (
            <>
              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>ID</TableCell>
                      <TableCell>Name</TableCell>
                      <TableCell>Email</TableCell>
                      <TableCell>Role</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell align="right">Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {pageData.content.map((user) => (
                      <TableRow key={user.id} hover>
                        <TableCell>{user.id}</TableCell>
                        <TableCell>{user.fullName}</TableCell>
                        <TableCell>{user.email}</TableCell>
                        <TableCell>
                          <Chip label={user.role} size="small" />
                        </TableCell>
                        <TableCell>
                          <Chip
                            label={user.enabled ? 'Active' : 'Inactive'}
                            color={user.enabled ? 'success' : 'default'}
                            size="small"
                          />
                        </TableCell>
                        <TableCell align="right">
                          <Tooltip title={user.enabled ? 'Deactivate' : 'Activate'}>
                            <span>
                              <IconButton
                                color={user.enabled ? 'error' : 'success'}
                                onClick={() => handleToggleStatus(user)}
                                disabled={actionId === user.id}
                              >
                                {user.enabled ? <BlockIcon /> : <CheckCircleIcon />}
                              </IconButton>
                            </span>
                          </Tooltip>
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

export default UsersPage;