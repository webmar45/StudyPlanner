import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { BarChart3, CalendarCheck2, BellRing, Timer, Sparkles, ListOrdered } from 'lucide-react'
import api from '../lib/api'
import StatCard from '../components/StatCard'

export default function DashboardPage() {
  const [analytics, setAnalytics] = useState(null)
  const [reminder, setReminder] = useState('')
  const [recommendations, setRecommendations] = useState([])
  const [insight, setInsight] = useState(null)
  const [insightLoading, setInsightLoading] = useState(false)

  const fetchData = async () => {
    const [analyticsRes, reminderRes, recRes] = await Promise.all([
      api.get('/analytics'),
      api.get('/notifications/reminder'),
      api.get('/recommendations/next-tasks?limit=5'),
    ])
    setAnalytics(analyticsRes.data)
    setReminder(reminderRes.data.message)
    setRecommendations(recRes.data)
  }

  const loadInsight = async () => {
    setInsightLoading(true)
    try {
      const { data } = await api.get('/llm/weekly-summary')
      setInsight(data)
    } catch {
      setInsight({ summary: 'Could not load insight.', source: 'error' })
    } finally {
      setInsightLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [])

  return (
    <div>
      <div className="rounded-3xl border border-fuchsia-500/20 bg-gradient-to-r from-fuchsia-500/10 via-violet-500/10 to-slate-900 p-6">
        <h1 className="flex items-center gap-2 text-3xl font-semibold">
          <BarChart3 className="h-8 w-8 text-fuchsia-300" />
          Focus tracker dashboard
        </h1>
        <p className="mt-3 inline-flex items-center gap-2 rounded-lg bg-slate-900/60 px-3 py-2 text-slate-300">
          <BellRing className="h-4 w-4 text-amber-300" />
          {reminder}
        </p>
      </div>
      <div className="mt-5 grid gap-4 md:grid-cols-4">
        <StatCard title="Today Focus (min)" value={analytics?.todayStudyMinutes || 0} />
        <StatCard title="Week Focus (min)" value={analytics?.weekStudyMinutes || 0} />
        <StatCard title="Completed Tasks" value={analytics?.completedTasks || 0} />
        <StatCard title="Pending Tasks" value={analytics?.pendingTasks || 0} />
      </div>
      <div className="mt-6 grid gap-4 md:grid-cols-3">
        <QuickLink to="/planner" title="Study Planner" desc="Plan tasks, priorities, and calendar." icon={CalendarCheck2} />
        <QuickLink to="/timer" title="Pomodoro Timer" desc="Start, pause and reset focus sessions." icon={Timer} />
        <QuickLink to="/analytics" title="Analytics" desc="Visualize trend insights and progress." icon={BarChart3} />
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <div className="rounded-2xl border border-violet-500/20 bg-slate-900 p-5">
          <h2 className="mb-3 flex items-center gap-2 text-lg font-semibold text-violet-200">
            <ListOrdered className="h-5 w-5" />
            Suggested next tasks
          </h2>
          {recommendations.length === 0 ? (
            <p className="text-sm text-slate-400">No pending tasks. Add some in the planner.</p>
          ) : (
            <ul className="space-y-2">
              {recommendations.map((r) => (
                <li key={r.taskId} className="rounded-lg border border-slate-700 bg-slate-800/80 px-3 py-2">
                  <p className="font-medium text-slate-100">{r.title}</p>
                  <p className="text-xs text-slate-400">
                    {r.subject} · {r.date} · {r.priority}
                  </p>
                  <p className="mt-1 text-xs text-fuchsia-300">{r.reason}</p>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="rounded-2xl border border-emerald-500/20 bg-slate-900 p-5">
          <h2 className="mb-3 flex items-center gap-2 text-lg font-semibold text-emerald-200">
            <Sparkles className="h-5 w-5" />
            Weekly insight
          </h2>
          <p className="mb-3 text-sm text-slate-400">
            Rule-based summary by default; set <code className="text-cyan-300">app.openai.api-key</code> for OpenAI wording.
          </p>
          <button
            type="button"
            onClick={loadInsight}
            disabled={insightLoading}
            className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-500 disabled:opacity-50"
          >
            {insightLoading ? 'Loading…' : 'Generate insight'}
          </button>
          {insight && (
            <div className="mt-4 rounded-lg border border-slate-700 bg-slate-800/80 p-3 text-sm text-slate-200">
              <p className="mb-2 whitespace-pre-line">{insight.summary}</p>
              <p className="text-xs text-slate-500">Source: {insight.source}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

function QuickLink({ to, title, desc, icon: Icon }) {
  return (
    <Link to={to} className="rounded-2xl border border-slate-700 bg-slate-900 p-5 shadow-lg transition hover:-translate-y-0.5 hover:border-fuchsia-500/40">
      <h3 className="flex items-center gap-2 text-lg font-semibold text-fuchsia-300">
        <Icon className="h-5 w-5" />
        {title}
      </h3>
      <p className="mt-2 text-sm text-slate-400">{desc}</p>
    </Link>
  )
}
