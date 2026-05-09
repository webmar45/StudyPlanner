import { Link } from 'react-router-dom'
import { CalendarCheck2, Clock3, Sparkles, TrendingUp } from 'lucide-react'

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-slate-950 text-white">
      <div className="mx-auto max-w-6xl px-6 py-20">
        <div className="rounded-3xl border border-fuchsia-400/20 bg-gradient-to-br from-fuchsia-600/20 via-slate-900 to-cyan-500/10 p-8 shadow-2xl md:p-14">
          <p className="mb-3 inline-flex items-center gap-2 text-sm font-medium uppercase tracking-[0.2em] text-cyan-300">
            <Sparkles className="h-4 w-4" />
            Smart Study Planner
          </p>
          <h1 className="text-4xl font-semibold leading-tight md:text-6xl">
            Plan smarter, focus deeper, and track your progress daily.
          </h1>
          <p className="mt-5 max-w-2xl text-slate-300">
            A premium productivity hub for students with AI-ready planning workflows, Pomodoro focus sessions, and
            visual analytics.
          </p>
          <div className="mt-8 flex flex-wrap gap-4">
            <Link className="rounded-xl bg-fuchsia-500 px-6 py-3 font-medium hover:bg-fuchsia-400" to="/register">
              Create Free Account
            </Link>
            <Link className="rounded-xl border border-white/20 px-6 py-3 font-medium hover:bg-white/10" to="/login">
              Login
            </Link>
          </div>
          <div className="mt-10 grid gap-3 text-sm text-slate-300 md:grid-cols-3">
            <Feature icon={CalendarCheck2} text="Smart daily planning" />
            <Feature icon={Clock3} text="Pomodoro focus flow" />
            <Feature icon={TrendingUp} text="Visual productivity insights" />
          </div>
        </div>
      </div>
    </div>
  )
}

function Feature({ icon: Icon, text }) {
  return (
    <div className="rounded-xl border border-slate-700 bg-slate-900/70 px-4 py-3">
      <p className="inline-flex items-center gap-2">
        <Icon className="h-4 w-4 text-cyan-300" />
        {text}
      </p>
    </div>
  )
}
