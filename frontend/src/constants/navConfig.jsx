import HomeIcon from '@mui/icons-material/Home';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import EventNoteIcon from '@mui/icons-material/EventNote';
import BeachAccessIcon from '@mui/icons-material/BeachAccess';
import SchoolIcon from '@mui/icons-material/School';
import DescriptionIcon from '@mui/icons-material/Description';
import AssignmentIcon from '@mui/icons-material/Assignment';
import DashboardIcon from '@mui/icons-material/Dashboard';
import GroupsIcon from '@mui/icons-material/Groups';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import CoPresentIcon from '@mui/icons-material/CoPresent';
import FactCheckIcon from '@mui/icons-material/FactCheck';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import MoreTimeIcon from '@mui/icons-material/MoreTime';
import BarChartIcon from '@mui/icons-material/BarChart';
import PeopleIcon from '@mui/icons-material/People';
import HowToRegIcon from '@mui/icons-material/HowToReg';

/** Roles permitted to use management screens — mirrors the backend rules. */
export const MANAGER_ROLES = ['SUPER_ADMIN', 'ADMIN'];

/**
 * The navigation map, organised into sections. Each link carries an icon and a
 * `show` predicate for role-gating. Adding a screen is one array entry; the
 * whole role-visibility policy is readable here. Gating is UX only — the
 * backend @PreAuthorize rules are the enforcement.
 */
export const NAV_SECTIONS = [
  {
    heading: null, // top group has no heading
    links: [
      { label: 'Home', path: '/', icon: HomeIcon, show: (user) => !!user },
    ],
  },
  {
    heading: 'Me',
    links: [
      { label: 'My Attendance', path: '/attendance', icon: AccessTimeIcon, show: (user) => !!user },
      { label: 'My Schedule', path: '/schedule', icon: EventNoteIcon, show: (user) => ['ADMIN', 'EMPLOYEE'].includes(user?.role) },
      { label: 'My Leaves', path: '/leaves', icon: BeachAccessIcon, show: (user) => !!user },
      { label: 'My Lectures', path: '/lectures', icon: SchoolIcon, show: (user) => user?.role === 'TEACHER' },
      { label: 'My Summaries', path: '/lecture-summaries', icon: DescriptionIcon, show: (user) => user?.role === 'TEACHER' },
      { label: 'My Reports', path: '/work-reports', icon: AssignmentIcon, show: (user) => ['EMPLOYEE', 'ADMIN', 'SUPER_ADMIN'].includes(user?.role) },
    ],
  },
  {
    heading: 'Management',
    links: [
      { label: 'Dashboard', path: '/admin/dashboard', icon: DashboardIcon, show: (user) => MANAGER_ROLES.includes(user?.role) },
      { label: 'Attendance', path: '/admin/attendance', icon: GroupsIcon, show: (user) => MANAGER_ROLES.includes(user?.role) },
      { label: 'Leaves', path: '/admin/leaves', icon: BeachAccessIcon, show: (user) => MANAGER_ROLES.includes(user?.role) },
      { label: 'Schedules', path: '/admin/schedules', icon: CalendarMonthIcon, show: (user) => MANAGER_ROLES.includes(user?.role) },
      { label: 'Lectures', path: '/admin/lectures', icon: CoPresentIcon, show: (user) => MANAGER_ROLES.includes(user?.role) },
      { label: 'Work Reports', path: '/admin/work-reports', icon: FactCheckIcon, show: (user) => MANAGER_ROLES.includes(user?.role) },
      { label: 'Lec. Summaries', path: '/admin/lecture-summaries', icon: MenuBookIcon, show: (user) => MANAGER_ROLES.includes(user?.role) },
      { label: 'Extensions', path: '/admin/deadline-extensions', icon: MoreTimeIcon, show: (user) => MANAGER_ROLES.includes(user?.role) },
      { label: 'Reports', path: '/admin/reports', icon: BarChartIcon, show: (user) => MANAGER_ROLES.includes(user?.role) },
      { label: 'Users', path: '/users', icon: PeopleIcon, show: (user) => MANAGER_ROLES.includes(user?.role) },
      { label: 'Registrations', path: '/admin/registrations', icon: HowToRegIcon, show: (user) => user?.role === 'SUPER_ADMIN' },
    ],
  },
];

/** Flattened list of all links (used to resolve the active page title). */
export const ALL_NAV_LINKS = NAV_SECTIONS.flatMap((section) => section.links);
