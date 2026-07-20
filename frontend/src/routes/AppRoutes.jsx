import { Routes, Route } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import HealthPage from '../pages/HealthPage';
import MyAttendancePage from '../pages/MyAttendancePage';
import AttendanceAdminPage from '../pages/AttendanceAdminPage';
import MyLeavesPage from '../pages/MyLeavesPage';
import LeaveAdminPage from '../pages/LeaveAdminPage';
import MyLecturesPage from '../pages/MyLecturesPage';
import LectureAdminPage from '../pages/LectureAdminPage';
import DashboardPage from '../pages/DashboardPage';
import ReportsPage from '../pages/ReportsPage';
import UsersPage from '../pages/UsersPage';
import CreateUserPage from '../pages/CreateUserPage';
import RegistrationAdminPage from '../pages/RegistrationAdminPage';
import MySchedulePage from '../pages/MySchedulePage';
import ScheduleAdminPage from '../pages/ScheduleAdminPage';
import MyWorkReportsPage from '../pages/MyWorkReportsPage';
import WorkReportsAdminPage from '../pages/WorkReportsAdminPage';
import MyLectureSummariesPage from '../pages/MyLectureSummariesPage';
import LectureSummariesAdminPage from '../pages/LectureSummariesAdminPage';
import DeadlineExtensionsAdminPage from '../pages/DeadlineExtensionsAdminPage';
import NotFoundPage from '../pages/NotFoundPage';

/**
 * Central route table — the app's complete navigational map. Every NAV_LINKS
 * entry in the Navbar must have a matching route here.
 */
const AppRoutes = () => {
  return (
    <Routes>
      {/* Public */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Protected — guarded once; children render via <Outlet /> */}
      <Route element={<ProtectedRoute />}>
        <Route path="/" element={<HealthPage />} />
        <Route path="/leaves" element={<MyLeavesPage />} />
        <Route path="/lectures" element={<MyLecturesPage />} />
        <Route path="/admin/dashboard" element={<DashboardPage />} />
        <Route path="/admin/attendance" element={<AttendanceAdminPage />} />
        <Route path="/admin/leaves" element={<LeaveAdminPage />} />
        <Route path="/admin/lectures" element={<LectureAdminPage />} />
        <Route path="/admin/reports" element={<ReportsPage />} />
        <Route path="/users" element={<UsersPage />} />
        <Route path="/users/new" element={<CreateUserPage />} />
        <Route path="/admin/registrations" element={<RegistrationAdminPage />} />
        <Route path="/schedule" element={<MySchedulePage />} />
        <Route path="/admin/schedules" element={<ScheduleAdminPage />} />
        <Route path="/work-reports" element={<MyWorkReportsPage />} />
        <Route path="/admin/work-reports" element={<WorkReportsAdminPage />} />
        <Route path="/lecture-summaries" element={<MyLectureSummariesPage />} />
        <Route path="/admin/lecture-summaries" element={<LectureSummariesAdminPage />} />
        <Route path="/admin/deadline-extensions" element={<DeadlineExtensionsAdminPage />} />
      </Route>

      {/* Attendance and leaves are not part of a teacher's workflow — their
          presence is tracked through lectures. Gate the routes so teachers
          can't reach them by URL, matching the hidden nav links. */}
      <Route element={<ProtectedRoute allowedRoles={['SUPER_ADMIN', 'ADMIN', 'EMPLOYEE']} />}>
        <Route path="/attendance" element={<MyAttendancePage />} />
        <Route path="/leaves" element={<MyLeavesPage />} />
      </Route>

      {/* Catch-all */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};

export default AppRoutes;
