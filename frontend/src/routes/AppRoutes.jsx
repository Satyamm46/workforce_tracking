import { Routes, Route } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';
import LoginPage from '../pages/LoginPage';
import HealthPage from '../pages/HealthPage';
import MyAttendancePage from '../pages/MyAttendancePage';
import AttendanceAdminPage from '../pages/AttendanceAdminPage';
import UsersPage from '../pages/UsersPage';
import CreateUserPage from '../pages/CreateUserPage';
import NotFoundPage from '../pages/NotFoundPage';

const AppRoutes = () => {
  return (
    <Routes>
      {/* Public */}
      <Route path="/login" element={<LoginPage />} />

      {/* Protected — guarded once; children render via <Outlet /> */}
      <Route element={<ProtectedRoute />}>
        <Route path="/" element={<HealthPage />} />
        <Route path="/attendance" element={<MyAttendancePage />} />
        <Route path="/admin/attendance" element={<AttendanceAdminPage />} />
        <Route path="/users" element={<UsersPage />} />
        <Route path="/users/new" element={<CreateUserPage />} />
      </Route>

      {/* Catch-all */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};

export default AppRoutes;