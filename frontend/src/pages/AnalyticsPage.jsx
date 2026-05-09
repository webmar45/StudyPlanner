import { useEffect, useMemo, useState } from 'react'
import dayjs from 'dayjs'
import { Bar, BarChart, CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { Activity, BarChart3, Layers, TrendingUp } from 'lucide-react'
import StatCard from '../components/StatCard'
import api from '../lib/api'

export default function AnalyticsPage() {
  const [forDate, setForDate] = useState(() => dayjs().format('YYYY-MM-DD'))
  const [analytics, setAnalytics] = useState(null)
  const focusData = useMemo(() => analytics?.focusBreakdown || [], [analytics])
  const tooltipStyle = {
    backgroundColor: '#0f172a',
    border: '1px solid #334155',
    borderRadius: '10px',
    color: '#e2e8f0',
  }
  const tooltipLabelStyle = { color: '#f8fafc', fontWeight: 700 }
  const tooltipItemStyle = { color: '#67e8f9' }

  useEffect(() => {
    api.get('/analytics', { params: { date: forDate } }).then((res) => setAnalytics(res.data))
  }, [forDate])

  const dayLabel = dayjs(forDate).isSame(dayjs(), 'day') ? 'Today' : dayjs(forDate).format('MMM D')

  return (
    <div>
      <div className="mb-5 flex flex-col gap-4 rounded-2xl border border-cyan-400/20 bg-gradient-to-r from-cyan-500/10 via-sky-500/10 to-slate-900 p-5 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="flex items-center gap-2 text-2xl font-semibold">
          <Activity className="h-6 w-6 text-cyan-300" />
          Productivity Analytics
        </h1>
        <label className="flex items-center gap-2 text-sm text-slate-300">
          <span>Day</span>
          <input
            type="date"
            value={forDate}
            onChange={(e) => setForDate(e.target.value)}
            className="rounded-lg border border-slate-600 bg-slate-900 px-3 py-2 text-slate-100"
          />
        </label>
      </div>
      <div className="grid gap-4 md:grid-cols-4">
        <StatCard title={`${dayLabel} focus (min)`} value={analytics?.todayStudyMinutes || 0} />
        <StatCard title="Week Focus (min)" value={analytics?.weekStudyMinutes || 0} />
        <StatCard title="Completed Tasks" value={analytics?.completedTasks || 0} />
        <StatCard title="Pending Tasks" value={analytics?.pendingTasks || 0} />
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <div className="h-80 rounded-2xl border border-violet-500/20 bg-slate-900 p-5">
          <h3 className="mb-4 flex items-center gap-2 text-lg font-semibold text-violet-200">
            <TrendingUp className="h-5 w-5" />
            Productivity Trend
          </h3>
          <ResponsiveContainer>
            <LineChart data={analytics?.productivityTrend || []}>
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
              <XAxis dataKey="day" stroke="#94a3b8" />
              <YAxis stroke="#94a3b8" />
              <Tooltip contentStyle={tooltipStyle} labelStyle={tooltipLabelStyle} itemStyle={tooltipItemStyle} />
              <Line type="monotone" dataKey="minutes" stroke="#22d3ee" strokeWidth={3} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="h-80 rounded-2xl border border-cyan-500/20 bg-slate-900 p-5">
          <h3 className="mb-4 flex items-center gap-2 text-lg font-semibold text-cyan-200">
            <BarChart3 className="h-5 w-5" />
            Daily Study Minutes
          </h3>
          <ResponsiveContainer>
            <BarChart data={analytics?.productivityTrend || []}>
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
              <XAxis dataKey="day" stroke="#94a3b8" />
              <YAxis stroke="#94a3b8" />
              <Tooltip contentStyle={tooltipStyle} labelStyle={tooltipLabelStyle} itemStyle={tooltipItemStyle} />
              <Bar dataKey="minutes" fill="#60a5fa" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <div className="h-96 rounded-2xl border border-emerald-500/25 bg-slate-900 p-5">
          <h3 className="mb-2 flex items-center gap-2 text-lg font-semibold text-emerald-200">
            <Layers className="h-5 w-5" />
            Focus breakdown ({dayLabel})
          </h3>
          <p className="mb-4 text-xs text-slate-400">
            Linked tasks and general focus (by activity) in one view — no double counting.
          </p>
          <ResponsiveContainer>
            <BarChart data={focusData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
              <XAxis dataKey="title" stroke="#94a3b8" tick={{ fontSize: 10 }} interval={0} angle={-16} textAnchor="end" height={85} />
              <YAxis stroke="#94a3b8" />
              <Tooltip contentStyle={tooltipStyle} labelStyle={tooltipLabelStyle} itemStyle={tooltipItemStyle} />
              <Bar dataKey="minutes" fill="#34d399" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="rounded-2xl border border-emerald-500/25 bg-slate-900 p-5">
          <h3 className="mb-2 flex items-center gap-2 text-lg font-semibold text-emerald-200">
            <Layers className="h-5 w-5" />
            Minutes by category
          </h3>
          <p className="mb-4 text-xs text-slate-400">
            <span className="text-cyan-300">Task</span> = timer linked to a planner task. <span className="text-amber-300">General</span> = not linked; split by activity name.
          </p>
          <div className="space-y-2">
            {focusData.length > 0 ? (
              focusData.map((item) => (
                <div key={item.rowKey} className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2">
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <p className="text-sm font-medium text-slate-100">{item.title}</p>
                      {item.subtitle ? (
                        <p className="mt-0.5 text-xs text-slate-400">{item.subtitle}</p>
                      ) : null}
                    </div>
                    <div className="text-right">
                      <span
                        className={`inline-block rounded px-2 py-0.5 text-[10px] font-semibold uppercase ${
                          item.segment === 'TASK' ? 'bg-cyan-500/20 text-cyan-200' : 'bg-amber-500/20 text-amber-200'
                        }`}
                      >
                        {item.segment === 'TASK' ? 'Task' : 'General'}
                      </span>
                      <p className="mt-1 text-sm font-semibold text-emerald-300">{item.minutes} min</p>
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <p className="text-sm text-slate-400">No focus sessions for this day.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
