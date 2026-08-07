import { lazy, Suspense } from 'react';
import { Routes, Route } from 'react-router-dom';
import { Box, CircularProgress } from '@mui/material';
import ProtectedRoute from './ProtectedRoute';
import LoginPage from '../pages/LoginPage';

/*
 * Every page below is code-split: Vite emits one chunk per page and the browser
 * downloads a page only when its route is first visited. This keeps the initial
 * download to the shell plus the login screen instead of all 22 pages at once.
 *
 * LoginPage is the one deliberate exception — it is the landing route for every
 * signed-out visitor, so splitting it would only add a round trip and a spinner
 * flash on the most-requested page.
 */
const RegisterPage = lazy(() => import('../pages/RegisterPage'));
const ForgotPasswordPage = lazy(() => import('../pages/ForgotPasswordPage'));
const HealthPage = lazy(() => import('../pages/HealthPage'));
const MyAttendancePage = lazy(() => import('../pages/MyAttendancePage'));
const AttendanceAdminPage = lazy(() => import('../pages/AttendanceAdminPage'));
const MyLeavesPage = lazy(() => import('../pages/MyLeavesPage'));
const LeaveAdminPage = lazy(() => import('../pages/LeaveAdminPage'));
const MyLecturesPage = lazy(() => import('../pages/MyLecturesPage'));
const LectureAdminPage = lazy(() => import('../pages/LectureAdminPage'));
const MyCalendarPage = lazy(() => import('../pages/MyCalendarPage'));
const LectureCalendarAdminPage = lazy(() => import('../pages/LectureCalendarAdminPage'));
const DashboardPage = lazy(() => import('../pages/DashboardPage'));
const ReportsPage = lazy(() => import('../pages/ReportsPage'));
const UsersPage = lazy(() => import('../pages/UsersPage'));
const CreateUserPage = lazy(() => import('../pages/CreateUserPage'));
const RegistrationAdminPage = lazy(() => import('../pages/RegistrationAdminPage'));
const MySchedulePage = lazy(() => import('../pages/MySchedulePage'));
const ScheduleAdminPage = lazy(() => import('../pages/ScheduleAdminPage'));
const MyWorkReportsPage = lazy(() => import('../pages/MyWorkReportsPage'));
const WorkReportsAdminPage = lazy(() => import('../pages/WorkReportsAdminPage'));
const MyLectureSummariesPage = lazy(() => import('../pages/MyLectureSummariesPage'));
const LectureSummariesAdminPage = lazy(() => import('../pages/LectureSummariesAdminPage'));
const DeadlineExtensionsAdminPage = lazy(() => import('../pages/DeadlineExtensionsAdminPage'));
const NotFoundPage = lazy(() => import('../pages/NotFoundPage'));

/** Shown for the moment a page chunk is in flight. */
const PageFallback = () => (
  <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
    <CircularProgress />
  </Box>
);

/**
 * Central route table — the app's complete navigational map. Every NAV_LINKS
 * entry in the Navbar must have a matching route here.
 */
const AppRoutes = () => {
  return (
    <Suspense fallback={<PageFallback />}>
      <Routes>
        {/* Public */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />

        {/* Protected — guarded once; children render via <Outlet /> */}
        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<HealthPage />} />
          <Route path="/lectures" element={<MyLecturesPage />} />
          <Route path="/calendar" element={<MyCalendarPage />} />
          <Route path="/admin/lecture-calendar" element={<LectureCalendarAdminPage />} />
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
            can't reach them by URL, matching the hidden nav links. These must
            not be duplicated in the block above: identical paths tie-break by
            declaration order, so an ungated copy would shadow this guard. */}
        <Route element={<ProtectedRoute allowedRoles={['SUPER_ADMIN', 'ADMIN', 'EMPLOYEE']} />}>
          <Route path="/attendance" element={<MyAttendancePage />} />
          <Route path="/leaves" element={<MyLeavesPage />} />
        </Route>

        {/* Catch-all */}
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Suspense>
  );
};

export default AppRoutes;
