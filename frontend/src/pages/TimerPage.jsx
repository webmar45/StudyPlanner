import { useEffect, useMemo, useState } from 'react'
import { ChevronLeft, ChevronRight, Pause, Play, RotateCcw, Timer, Link2, Wrench } from 'lucide-react'
import api from '../lib/api'

export default function TimerPage() {
  const [seconds, setSeconds] = useState(25 * 60)
  const [running, setRunning] = useState(false)
  const [isBreak, setIsBreak] = useState(false)
  const [activeSession, setActiveSession] = useState(false)
  const [tasks, setTasks] = useState([])
  const [linkedTaskId, setLinkedTaskId] = useState('GENERAL_ACTIVITY')
  const [timerError, setTimerError] = useState('')

  const maxSeconds = isBreak ? 5 * 60 : 25 * 60
  const progress = ((maxSeconds - seconds) / maxSeconds) * 100

  useEffect(() => {
    api.get('/tasks').then((res) => setTasks(res.data)).catch(() => setTasks([]))
  }, [])

  const linkableTasks = useMemo(
    () => tasks.filter((t) => t.status !== 'COMPLETED'),
    [tasks],
  )

  useEffect(() => {
    if (linkedTaskId === 'GENERAL_ACTIVITY') return
    const stillLinkable = linkableTasks.some((t) => t.id === linkedTaskId)
    if (!stillLinkable) setLinkedTaskId('GENERAL_ACTIVITY')
  }, [linkableTasks, linkedTaskId])

  useEffect(() => {
    if (!running) return
    const t = setInterval(() => setSeconds((prev) => (prev > 0 ? prev - 1 : 0)), 1000)
    return () => clearInterval(t)
  }, [running])

  useEffect(() => {
    if (seconds !== 0) return
    const autoSwitch = async () => {
      if (!isBreak && activeSession) {
        try {
          await api.post('/session/end')
        } catch {
          setTimerError('Could not sync session end with server. Timer continues locally.')
        }
        setActiveSession(false)
      }
      const nextIsBreak = !isBreak
      setIsBreak(nextIsBreak)
      setSeconds(nextIsBreak ? 5 * 60 : 25 * 60)
      setRunning(false)
    }
    autoSwitch()
  }, [seconds, isBreak, activeSession])

  const start = async () => {
    setTimerError('')
    setRunning(true)
    if (!isBreak && !activeSession) {
      const linkedTask = linkableTasks.find((task) => task.id === linkedTaskId)
      const activityName = linkedTask ? `${linkedTask.title} (${linkedTask.subject})` : 'General Activity'
      try {
        await api.post('/session/start', {
          activityName,
          taskId: linkedTask ? linkedTask.id : null,
        })
        setActiveSession(true)
      } catch (err) {
        const message = err?.response?.data?.message || ''
        if (message.toLowerCase().includes('active study session already exists')) {
          setActiveSession(true)
        } else {
          setTimerError('Server session could not be started. Timer continues locally.')
        }
      }
    }
  }

  const reset = async () => {
    setTimerError('')
    if (activeSession) {
      try {
        await api.post('/session/end')
      } catch {
        setTimerError('Could not sync reset with server. Local timer has been reset.')
      }
      setActiveSession(false)
    }
    setRunning(false)
    setIsBreak(false)
    setSeconds(25 * 60)
  }

  const switchMode = async (nextIsBreak) => {
    setTimerError('')
    if (activeSession && !isBreak && nextIsBreak) {
      try {
        await api.post('/session/end')
      } catch {
        setTimerError('Could not sync mode switch with server. Timer switched locally.')
      }
      setActiveSession(false)
    }
    setRunning(false)
    setIsBreak(nextIsBreak)
    setSeconds(nextIsBreak ? 5 * 60 : 25 * 60)
  }

  const recoverSession = async () => {
    setTimerError('')
    try {
      await api.post('/session/end')
      setActiveSession(false)
      setRunning(false)
      setIsBreak(false)
      setSeconds(25 * 60)
    } catch {
      setTimerError('No active backend session found to recover, or server is unreachable.')
    }
  }

  return (
    <div className="rounded-3xl border border-fuchsia-400/20 bg-gradient-to-br from-slate-900 via-violet-950/30 to-slate-900 p-6 shadow-2xl md:p-8">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="flex items-center gap-2 text-2xl font-semibold md:text-3xl">
          <Timer className="h-7 w-7 text-fuchsia-300" />
          Pomodoro Timer
        </h1>
        <div className="flex items-center gap-2">
          <button
            onClick={() => switchMode(false)}
            className={`rounded-full p-2 transition ${!isBreak ? 'bg-fuchsia-500/20 text-fuchsia-200' : 'bg-slate-800 text-slate-300 hover:bg-slate-700'}`}
            aria-label="Switch to focus timer"
          >
            <ChevronLeft className="h-5 w-5" />
          </button>
          <button
            onClick={() => switchMode(true)}
            className={`rounded-full p-2 transition ${isBreak ? 'bg-cyan-500/20 text-cyan-200' : 'bg-slate-800 text-slate-300 hover:bg-slate-700'}`}
            aria-label="Switch to break timer"
          >
            <ChevronRight className="h-5 w-5" />
          </button>
        </div>
      </div>

      <p className="text-sm text-slate-300">
        Current mode: <span className={isBreak ? 'text-cyan-300' : 'text-fuchsia-300'}>{isBreak ? '5-minute Break' : '25-minute Focus'}</span>
      </p>
      {!!timerError && <p className="mt-3 rounded-lg bg-amber-500/20 px-3 py-2 text-sm text-amber-200">{timerError}</p>}

      <div className="mt-5 rounded-2xl border border-slate-700 bg-slate-900/80 p-4">
        <label className="mb-2 flex items-center gap-2 text-sm text-slate-300">
          <Link2 className="h-4 w-4 text-emerald-300" />
          Link this session to task
        </label>
        <select
          value={linkedTaskId}
          onChange={(e) => setLinkedTaskId(e.target.value)}
          className="w-full rounded-xl border border-slate-700 bg-slate-800 px-3 py-2 text-sm outline-none focus:border-fuchsia-400"
        >
          <option value="GENERAL_ACTIVITY">General Activity (default)</option>
          {linkableTasks.map((task) => (
            <option key={task.id} value={task.id}>
              {task.title} - {task.subject}
            </option>
          ))}
        </select>
      </div>

      <div className="mt-8 flex flex-col items-center">
        <div
          className="grid h-56 w-56 place-items-center rounded-full border-[10px] border-slate-800 bg-slate-950 text-center"
          style={{ background: `conic-gradient(${isBreak ? '#22d3ee' : '#e879f9'} ${progress}%, #1e293b ${progress}% 100%)` }}
        >
          <div className="grid h-44 w-44 place-items-center rounded-full bg-slate-900">
            <p className={`text-5xl font-semibold ${isBreak ? 'text-cyan-300' : 'text-fuchsia-300'}`}>{formatTime(seconds)}</p>
          </div>
        </div>
      </div>

      <div className="mt-8 flex flex-wrap justify-center gap-3">
        <button onClick={start} className="inline-flex items-center gap-2 rounded-xl bg-fuchsia-500 px-5 py-3 font-medium text-white transition hover:bg-fuchsia-400">
          <Play className="h-4 w-4" /> Start
        </button>
        <button onClick={() => setRunning(false)} className="inline-flex items-center gap-2 rounded-xl bg-amber-500 px-5 py-3 font-medium text-white transition hover:bg-amber-400">
          <Pause className="h-4 w-4" /> Pause
        </button>
        <button onClick={reset} className="inline-flex items-center gap-2 rounded-xl bg-slate-700 px-5 py-3 font-medium transition hover:bg-slate-600">
          <RotateCcw className="h-4 w-4" /> Reset
        </button>
        <button
          onClick={recoverSession}
          className="inline-flex items-center gap-2 rounded-xl bg-emerald-600 px-5 py-3 font-medium text-white transition hover:bg-emerald-500"
        >
          <Wrench className="h-4 w-4" /> Recover Session
        </button>
      </div>
    </div>
  )
}

function formatTime(totalSeconds) {
  const min = String(Math.floor(totalSeconds / 60)).padStart(2, '0')
  const sec = String(totalSeconds % 60).padStart(2, '0')
  return `${min}:${sec}`
}
