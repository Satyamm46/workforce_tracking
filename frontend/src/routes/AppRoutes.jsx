import { Routes, Route } from 'react-router-dom';
import HealthPage from '../pages/HealthPage';
import NotFoundPage from '../pages/NotFoundPage';

/**
 * Central route table for the application.
 *
 * Maps URL paths to the page components that render them. Keeping every route
 * in ONE place means the app's navigational structure can be understood at a
 * glance and extended in a single, predictable location as modules are added.
 *
 * The <BrowserRouter> that enables routing is mounted higher up, in main.jsx,
 * so this component only declares the route-to-page mapping.
 */
const AppRoutes = () => {
  return (
    <Routes>
      {/* Home: the system health screen (the app's only screen for now). */}
      <Route path="/" element={<HealthPage />} />

      {/* Catch-all: any unmatched URL renders the 404 page. */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};

export default AppRoutes;