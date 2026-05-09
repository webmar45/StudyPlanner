import { Navigate, Route, Routes } from 'react-router-dom'
import AppShell from './components/AppShell'
import ProtectedRoute from './components/ProtectedRoute'
import { useAuth } from './context/AuthContext'
import AnalyticsPage from './pages/AnalyticsPage'
import AuthPage from './pages/AuthPage'
import DashboardPage from './pages/DashboardPage'
import LandingPage from './pages/LandingPage'
import StudyPlannerPage from './pages/StudyPlannerPage'
import TimerPage from './pages/TimerPage'

function App() {
  const { isAuthenticated } = useAuth()

  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={isAuthenticated ? <Navigate to="/dashboard" /> : <AuthPage mode="login" />} />
      <Route path="/register" element={isAuthenticated ? <Navigate to="/dashboard" /> : <AuthPage mode="register" />} />
      <Route
        element={
          <ProtectedRoute>
            <AppShell />
          </ProtectedRoute>
        }
      >
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/planner" element={<StudyPlannerPage />} />
        <Route path="/timer" element={<TimerPage />} />
        <Route path="/analytics" element={<AnalyticsPage />} />
      </Route>
      <Route
        path="*"
        element={
          <Navigate to={isAuthenticated ? '/dashboard' : '/'} />
        }
      />
    </Routes>
  )
}

export default App
