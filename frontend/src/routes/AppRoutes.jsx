import { Routes, Route } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';
import LoginPage from '../pages/LoginPage';
import HealthPage from '../pages/HealthPage';
import NotFoundPage from '../pages/NotFoundPage';

const AppRoutes = () => {
  return (
    <Routes>
      {/* Public */}
      <Route path="/login" element={<LoginPage />} />

      {/* Protected — guarded once; children render via <Outlet /> */}
      <Route element={<ProtectedRoute />}>
        <Route path="/" element={<HealthPage />} />
      </Route>

      {/* Catch-all */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};

export default AppRoutes;