export default function StatCard({ title, value }) {
  return (
    <div className="rounded-2xl border border-fuchsia-500/20 bg-gradient-to-br from-slate-900 via-slate-900 to-violet-950/30 p-4 shadow-lg">
      <p className="text-sm text-slate-400">{title}</p>
      <p className="mt-2 text-2xl font-semibold text-fuchsia-300">{value}</p>
    </div>
  )
}
