import dayjs from 'dayjs'
import { useEffect, useMemo, useState } from 'react'
import {
  BookOpen,
  CalendarDays,
  CheckCircle2,
  ClipboardList,
  ExternalLink,
  Filter,
  GraduationCap,
  Loader2,
  PlusCircle,
  Trash2,
  PlayCircle,
  Wand2,
  X,
} from 'lucide-react'
import api from '../lib/api'

const initialTask = { title: '', subject: '', date: dayjs().format('YYYY-MM-DD'), duration: 60, priority: 'MEDIUM', status: 'PENDING' }

export default function StudyPlannerPage() {
  const [tasks, setTasks] = useState([])
  const [form, setForm] = useState(initialTask)
  const [filter, setFilter] = useState({ date: '', subject: '', priority: '' })
  const [quickText, setQuickText] = useState('')
  const [resourceTask, setResourceTask] = useState(null)
  const [resources, setResources] = useState({ loading: false, data: null, error: null })

  const load = async () => {
    const { data } = await api.get('/tasks')
    setTasks(data)
  }

  useEffect(() => {
    load()
  }, [])

  const createTask = async (e) => {
    e.preventDefault()
    await api.post('/tasks', form)
    setForm(initialTask)
    load()
  }

  const toggle = async (task) => {
    await api.patch(`/tasks/${task.id}/complete`, { completed: task.status !== 'COMPLETED' })
    load()
  }

  const remove = async (id) => {
    await api.delete(`/tasks/${id}`)
    if (resourceTask?.id === id) {
      setResourceTask(null)
      setResources({ loading: false, data: null, error: null })
    }
    load()
  }

  const loadLearningResources = async (task) => {
    setResourceTask(task)
    setResources({ loading: true, data: null, error: null })
    try {
      const { data } = await api.get(`/tasks/${task.id}/learning-resources`)
      setResources({ loading: false, data, error: null })
    } catch (e) {
      setResources({
        loading: false,
        data: null,
        error: e.response?.data?.message || e.message || 'Could not load resources',
      })
    }
  }

  const closeResources = () => {
    setResourceTask(null)
    setResources({ loading: false, data: null, error: null })
  }

  const parseQuickAdd = async () => {
    if (!quickText.trim()) return
    const { data } = await api.post('/llm/quick-add', { text: quickText })
    const d = typeof data.date === 'string' ? data.date.slice(0, 10) : data.date
    setForm({
      title: data.title,
      subject: data.subject,
      date: d,
      duration: data.duration,
      priority: data.priority,
      status: data.status,
    })
  }

  const filtered = useMemo(
    () =>
      tasks.filter((t) => {
        if (filter.date && t.date !== filter.date) return false
        if (filter.subject && t.subject.toLowerCase() !== filter.subject.toLowerCase()) return false
        if (filter.priority && t.priority !== filter.priority) return false
        return true
      }),
    [tasks, filter],
  )

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <div className="rounded-2xl border border-cyan-500/25 bg-slate-900 p-5 lg:col-span-2">
        <h2 className="mb-2 flex items-center gap-2 text-lg font-semibold text-cyan-200">
          <Wand2 className="h-5 w-5" />
          Quick add (smart parse)
        </h2>
        <p className="mb-3 text-sm text-slate-400">
          Example: <span className="text-slate-300">Math - Chapter 5 high priority 45 min tomorrow</span>
        </p>
        <div className="flex flex-col gap-2 sm:flex-row">
          <input
            value={quickText}
            onChange={(e) => setQuickText(e.target.value)}
            placeholder="Describe your task in plain language…"
            className="flex-1 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm"
          />
          <button
            type="button"
            onClick={parseQuickAdd}
            className="rounded-lg bg-cyan-600 px-4 py-2 text-sm font-medium hover:bg-cyan-500"
          >
            Parse into form
          </button>
        </div>
      </div>

      <form onSubmit={createTask} className="rounded-2xl border border-violet-500/20 bg-slate-900 p-5">
        <h2 className="flex items-center gap-2 text-xl font-semibold text-violet-200">
          <PlusCircle className="h-5 w-5" />
          Create Study Task
        </h2>
        <div className="mt-4 grid gap-3">
          <input required className="rounded-lg bg-slate-800 p-2" placeholder="Title" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
          <input required className="rounded-lg bg-slate-800 p-2" placeholder="Subject" value={form.subject} onChange={(e) => setForm({ ...form, subject: e.target.value })} />
          <input type="date" className="rounded-lg bg-slate-800 p-2" value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} />
          <input type="number" min={5} className="rounded-lg bg-slate-800 p-2" value={form.duration} onChange={(e) => setForm({ ...form, duration: Number(e.target.value) })} />
          <select className="rounded-lg bg-slate-800 p-2" value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value })}>
            <option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">High</option>
          </select>
          <button className="rounded-lg bg-violet-500 px-4 py-2 font-medium hover:bg-violet-400">Add Task</button>
        </div>
      </form>

      <div className="rounded-2xl border border-cyan-500/20 bg-slate-900 p-5">
        <h2 className="flex items-center gap-2 text-xl font-semibold text-cyan-200">
          <CalendarDays className="h-5 w-5" />
          Calendar View (Today)
        </h2>
        <div className="mt-3 space-y-2">
          {tasks.filter((t) => t.date === dayjs().format('YYYY-MM-DD')).map((task) => (
            <div key={task.id} className="rounded-lg border border-slate-700 bg-slate-800 p-3">
              <p className="font-medium">{task.title}</p>
              <p className="text-xs text-slate-400">{task.subject} | {task.priority} | {task.duration} min</p>
            </div>
          ))}
          {!tasks.some((t) => t.date === dayjs().format('YYYY-MM-DD')) && <p className="text-sm text-slate-400">No tasks for today.</p>}
        </div>
      </div>

      <div className="rounded-2xl border border-slate-700 bg-slate-900 p-5 lg:col-span-2">
        <div className="mb-4 flex items-center gap-2 text-sm text-slate-300">
          <Filter className="h-4 w-4 text-fuchsia-300" />
          Filter tasks
        </div>
        <div className="mb-4 flex flex-wrap gap-2">
          <input className="rounded bg-slate-800 p-2 text-sm" placeholder="Date YYYY-MM-DD" onChange={(e) => setFilter({ ...filter, date: e.target.value })} />
          <input className="rounded bg-slate-800 p-2 text-sm" placeholder="Subject" onChange={(e) => setFilter({ ...filter, subject: e.target.value })} />
          <select className="rounded bg-slate-800 p-2 text-sm" onChange={(e) => setFilter({ ...filter, priority: e.target.value })}>
            <option value="">All priorities</option><option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">High</option>
          </select>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="text-slate-400"><tr><th className="inline-flex items-center gap-2"><ClipboardList className="h-4 w-4" />Title</th><th>Subject</th><th>Date</th><th>Priority</th><th>Status</th><th /></tr></thead>
            <tbody>
              {filtered.map((task) => (
                <tr key={task.id} className="border-t border-slate-800">
                  <td className="py-2">{task.title}</td><td>{task.subject}</td><td>{task.date}</td><td>{task.priority}</td><td>{task.status}</td>
                  <td className="space-x-2">
                    <button
                      type="button"
                      onClick={() => loadLearningResources(task)}
                      className="inline-flex items-center gap-1 rounded bg-sky-600 px-2 py-1 text-xs hover:bg-sky-500"
                    >
                      <GraduationCap className="h-3 w-3" />
                      Resources
                    </button>
                    <button onClick={() => toggle(task)} className="inline-flex items-center gap-1 rounded bg-emerald-600 px-2 py-1 text-xs hover:bg-emerald-500"><CheckCircle2 className="h-3 w-3" />Toggle</button>
                    <button onClick={() => remove(task.id)} className="inline-flex items-center gap-1 rounded bg-rose-600 px-2 py-1 text-xs hover:bg-rose-500"><Trash2 className="h-3 w-3" />Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {resourceTask && (
        <div className="rounded-2xl border border-sky-500/30 bg-slate-900 p-5 lg:col-span-2">
          <div className="mb-4 flex flex-wrap items-start justify-between gap-2">
            <div>
              <h2 className="flex items-center gap-2 text-lg font-semibold text-sky-200">
                <GraduationCap className="h-5 w-5" />
                Study resources
              </h2>
              <p className="mt-1 text-sm text-slate-400">
                <span className="text-slate-300">{resourceTask.title}</span>
                <span className="mx-1">·</span>
                {resourceTask.subject}
              </p>
              {resources.data?.queryUsed && (
                <p className="mt-1 text-xs text-slate-500">Search: {resources.data.queryUsed}</p>
              )}
            </div>
            <button
              type="button"
              onClick={closeResources}
              className="rounded-lg border border-slate-600 p-2 text-slate-400 hover:bg-slate-800 hover:text-slate-200"
              aria-label="Close resources"
            >
              <X className="h-4 w-4" />
            </button>
          </div>

          {resources.loading && (
            <div className="flex items-center gap-2 text-sm text-slate-400">
              <Loader2 className="h-4 w-4 animate-spin" />
              Loading YouTube and Wikipedia…
            </div>
          )}

          {resources.error && (
            <p className="rounded-lg border border-rose-500/40 bg-rose-950/40 px-3 py-2 text-sm text-rose-200">{resources.error}</p>
          )}

          {resources.data && !resources.loading && (
            <div className="grid gap-6 lg:grid-cols-2">
              <div>
                <h3 className="mb-3 flex items-center gap-2 text-sm font-semibold uppercase tracking-wide text-slate-400">
                  <PlayCircle className="h-4 w-4 text-red-400" />
                  Related videos
                </h3>
                {resources.data.youtubeVideos?.length === 0 && (
                  <p className="mb-3 text-sm text-slate-500">Use the YouTube search link below.</p>
                )}
                <ul className="space-y-3">
                  {resources.data.youtubeVideos?.map((v) => (
                    <li key={v.videoId} className="rounded-xl border border-slate-700/80 bg-slate-800/50 p-3">
                      <a
                        href={v.watchUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex gap-3 transition hover:opacity-95"
                      >
                        {v.thumbnailUrl ? (
                          <img src={v.thumbnailUrl} alt="" className="h-20 w-36 shrink-0 rounded-lg object-cover" />
                        ) : (
                          <div className="flex h-20 w-36 shrink-0 items-center justify-center rounded-lg bg-slate-700 text-xs text-slate-500">
                            No thumb
                          </div>
                        )}
                        <div className="min-w-0 flex-1">
                          <p className="line-clamp-2 text-sm font-medium text-slate-100">{v.title}</p>
                          <p className="mt-0.5 text-xs text-slate-500">{v.channelTitle}</p>
                          <span className="mt-1 inline-flex items-center gap-1 text-xs text-sky-400">
                            Watch on YouTube <ExternalLink className="h-3 w-3" />
                          </span>
                        </div>
                      </a>
                      <a
                        href={v.watchUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="mt-2 block break-all text-xs text-slate-500 underline decoration-slate-600 underline-offset-2 hover:text-sky-400"
                      >
                        {v.watchUrl}
                      </a>
                    </li>
                  ))}
                </ul>
                {resources.data.queryUsed && (
                  <p className="mt-4 text-xs text-slate-500">
                    More on YouTube:{' '}
                    <a
                      href={`https://www.youtube.com/results?search_query=${encodeURIComponent(resources.data.queryUsed)}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="break-all text-sky-400 underline decoration-sky-500/40 hover:text-sky-300"
                    >
                      youtube.com/results?search_query={encodeURIComponent(resources.data.queryUsed)}
                    </a>
                  </p>
                )}
              </div>

              <div>
                <h3 className="mb-3 flex items-center gap-2 text-sm font-semibold uppercase tracking-wide text-slate-400">
                  <BookOpen className="h-4 w-4 text-slate-300" />
                  Wikipedia
                </h3>
                {resources.data.wikipediaArticles?.length === 0 && (
                  <p className="text-sm text-slate-500">No Wikipedia articles matched. Try a clearer subject or title.</p>
                )}
                <ul className="space-y-4">
                  {resources.data.wikipediaArticles?.map((a) => (
                    <li key={a.title} className="rounded-xl border border-slate-700/80 bg-slate-800/40 p-3">
                      <a
                        href={a.articleUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="group flex items-start justify-between gap-2"
                      >
                        <span className="font-medium text-sky-300 group-hover:underline">{a.title}</span>
                        <ExternalLink className="h-4 w-4 shrink-0 text-slate-500 group-hover:text-sky-400" />
                      </a>
                      {(a.extract || a.articleUrl) && (
                        <p className="mt-2 text-sm leading-relaxed text-slate-400">{a.extract || 'Read more on Wikipedia.'}</p>
                      )}
                      {a.articleUrl && (
                        <a
                          href={a.articleUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="mt-2 block break-all text-xs text-slate-500 underline decoration-slate-600 underline-offset-2 hover:text-sky-400"
                        >
                          {a.articleUrl}
                        </a>
                      )}
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
