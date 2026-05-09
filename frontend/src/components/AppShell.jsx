import { Link, Outlet, useLocation } from 'react-router-dom'
import { BarChart3, CalendarCheck2, LayoutDashboard, Timer, LogOut } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

const nav = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/planner', label: 'Study Planner', icon: CalendarCheck2 },
  { to: '/timer', label: 'Timer', icon: Timer },
  { to: '/analytics', label: 'Analytics', icon: BarChart3 },
]

export default function AppShell() {
  const { pathname } = useLocation()
  const { logout, user } = useAuth()

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100">
      <header className="sticky top-0 z-20 border-b border-fuchsia-400/10 bg-slate-950/85 backdrop-blur">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3">
          <p className="bg-gradient-to-r from-cyan-300 to-fuchsia-300 bg-clip-text font-semibold text-transparent">Smart Study Planner</p>
          <nav className="hidden gap-2 md:flex">
            {nav.map((item) => {
              const Icon = item.icon
              return (
                <Link
                  key={item.to}
                  to={item.to}
                  className={`inline-flex items-center gap-2 rounded-lg px-3 py-2 text-sm transition ${pathname === item.to ? 'bg-fuchsia-500/20 text-fuchsia-200' : 'text-slate-300 hover:bg-slate-800'}`}
                >
                  <Icon className="h-4 w-4" />
                  {item.label}
                </Link>
              )
            })}
          </nav>
          <div className="flex items-center gap-3">
            <span className="hidden text-sm text-slate-300 sm:block">{user?.name}</span>
            <button onClick={logout} className="inline-flex items-center gap-1 rounded-lg bg-slate-800 px-3 py-2 text-sm hover:bg-slate-700">
              <LogOut className="h-4 w-4" />
              Logout
            </button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-7xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  )
}
