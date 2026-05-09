import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { LockKeyhole, Mail, UserRound } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

export default function AuthPage({ mode }) {
  const isLogin = mode === 'login'
  const navigate = useNavigate()
  const { login, register } = useAuth()
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState('')

  const onSubmit = async (e) => {
    e.preventDefault()
    try {
      if (isLogin) {
        await login({ email: form.email, password: form.password })
      } else {
        await register(form)
      }
      navigate('/dashboard')
    } catch (err) {
      setError(err?.response?.data?.message || 'Something went wrong')
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-950 p-4">
      <form onSubmit={onSubmit} className="w-full max-w-md rounded-2xl border border-fuchsia-500/20 bg-slate-900/85 p-7 shadow-2xl">
        <h2 className="text-2xl font-semibold text-white">{isLogin ? 'Welcome back' : 'Create account'}</h2>
        <p className="mt-1 text-sm text-slate-400">{isLogin ? 'Continue your study streak.' : 'Start your productivity journey.'}</p>
        {!!error && <p className="mt-3 rounded bg-red-500/20 p-2 text-sm text-red-300">{error}</p>}
        {!isLogin && (
          <div className="relative mt-4">
            <UserRound className="pointer-events-none absolute left-3 top-3.5 h-4 w-4 text-slate-400" />
            <input
              placeholder="Full name"
              className="w-full rounded-lg border border-slate-700 bg-slate-800 py-3 pl-10 pr-3 text-sm outline-none focus:border-fuchsia-400"
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
            />
          </div>
        )}
        <div className="relative mt-3">
          <Mail className="pointer-events-none absolute left-3 top-3.5 h-4 w-4 text-slate-400" />
          <input
            type="email"
            placeholder="Email"
            className="w-full rounded-lg border border-slate-700 bg-slate-800 py-3 pl-10 pr-3 text-sm outline-none focus:border-fuchsia-400"
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            required
          />
        </div>
        <div className="relative mt-3">
          <LockKeyhole className="pointer-events-none absolute left-3 top-3.5 h-4 w-4 text-slate-400" />
          <input
            type="password"
            placeholder="Password"
            className="w-full rounded-lg border border-slate-700 bg-slate-800 py-3 pl-10 pr-3 text-sm outline-none focus:border-fuchsia-400"
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            required
          />
        </div>
        <button className="mt-4 w-full rounded-lg bg-fuchsia-500 p-3 font-medium hover:bg-fuchsia-400" type="submit">
          {isLogin ? 'Login' : 'Register'}
        </button>
        <p className="mt-4 text-center text-sm text-slate-400">
          {isLogin ? 'No account?' : 'Already have an account?'}{' '}
          <Link className="text-cyan-300" to={isLogin ? '/register' : '/login'}>
            {isLogin ? 'Register' : 'Login'}
          </Link>
        </p>
      </form>
    </div>
  )
}
