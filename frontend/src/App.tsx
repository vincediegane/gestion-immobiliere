import { useCallback, useEffect, useMemo, useState, type FormEvent, type ReactNode } from 'react'
import { BrowserRouter, Navigate, NavLink, Route, Routes, useNavigate } from 'react-router-dom'
import { Alert, Autocomplete, Button, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Skeleton, TextField } from '@mui/material'
import { api, session, type Page, xof } from './api'

type Entity = Record<string, unknown> & { id: string }
type Lookup = { endpoint: string; labelKey: string; secondaryKey?: string }
type Field = { key: string; label: string; type?: string; options?: string[]; lookup?: Lookup }
type LoginResult = { accessToken: string; fullName: string; role: string }

const nav = [
  ['/', 'Tableau de bord', 'Vue'],
  ['/owners', 'Propriétaires', 'Owners'],
  ['/properties', 'Biens', 'Assets'],
  ['/units', 'Unités', 'Units'],
  ['/tenants', 'Locataires', 'Tenants'],
  ['/leases', 'Baux', 'Leases'],
  ['/billing', 'Loyers', 'Billing'],
]

const statusTone: Record<string, string> = {
  ACTIVE: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
  AVAILABLE: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
  PAID: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
  OCCUPIED: 'bg-sky-50 text-sky-700 ring-sky-200',
  DUE: 'bg-amber-50 text-amber-700 ring-amber-200',
  PARTIAL: 'bg-amber-50 text-amber-700 ring-amber-200',
  OVERDUE: 'bg-rose-50 text-rose-700 ring-rose-200',
  MAINTENANCE: 'bg-slate-100 text-slate-700 ring-slate-200',
  TERMINATED: 'bg-slate-100 text-slate-700 ring-slate-200',
  UPCOMING: 'bg-indigo-50 text-indigo-700 ring-indigo-200',
}

function usePage(endpoint: string) {
  const [rows, setRows] = useState<Entity[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const load = useCallback(() => {
    setLoading(true)
    setError('')
    void api<Page<Entity>>(`${endpoint}?size=100`)
      .then(page => setRows(page.content))
      .catch(e => setError((e as Error).message))
      .finally(() => setLoading(false))
  }, [endpoint])
  useEffect(() => { load() }, [load])
  return { rows, loading, error, setError, load }
}

function useEntities(endpoint: string) {
  const [rows, setRows] = useState<Entity[]>([])
  useEffect(() => { api<Page<Entity>>(`${endpoint}?size=100`).then(page => setRows(page.content)).catch(() => setRows([])) }, [endpoint])
  return rows
}

function Login() {
  const navigate = useNavigate()
  const [error, setError] = useState('')
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    try {
      const result = await api<LoginResult>('/api/auth/login', { method: 'POST', body: JSON.stringify({ email: data.get('email'), password: data.get('password') }) })
      session.save(result.accessToken, result.fullName, result.role)
      navigate('/')
    } catch (e) {
      setError((e as Error).message)
    }
  }
  return (
    <main className="grid min-h-screen place-items-center px-4 py-10">
      <section className="w-full max-w-5xl overflow-hidden rounded-[2rem] border border-white/70 bg-white/80 shadow-2xl shadow-slate-200 backdrop-blur">
        <div className="grid md:grid-cols-[1.1fr_0.9fr]">
          <div className="hidden bg-slate-950 p-10 text-white md:block">
            <p className="text-sm font-semibold uppercase tracking-[0.35em] text-teal-300">Gestion Sénégal</p>
            <h1 className="mt-6 text-4xl font-black leading-tight">Pilotez vos loyers, baux et quittances depuis un cockpit clair.</h1>
            <p className="mt-5 max-w-md text-slate-300">Un MVP SaaS pensé pour les gestionnaires immobiliers: référentiel, impayés, relances WhatsApp et quittances PDF.</p>
            <div className="mt-10 grid grid-cols-3 gap-3 text-sm">
              {['JWT', 'PostgreSQL', 'PDF'].map(item => <div key={item} className="rounded-2xl bg-white/10 p-4 font-semibold">{item}</div>)}
            </div>
          </div>
          <div className="p-8 md:p-10">
            <p className="text-sm font-semibold text-teal-700">Real Estate SaaS MVP</p>
            <h2 className="mt-2 text-3xl font-black text-slate-950">Connexion</h2>
            <p className="mt-2 text-slate-500">Accédez à l’espace gestionnaire de votre organisation.</p>
            {error && <Alert severity="error" className="mt-6">{error}</Alert>}
            <form onSubmit={submit} className="mt-8 space-y-5">
              <TextField name="email" label="Email" autoComplete="username" required fullWidth />
              <TextField name="password" label="Mot de passe" type="password" autoComplete="current-password" required fullWidth />
              <Button type="submit" variant="contained" size="large" fullWidth>Se connecter</Button>
            </form>
          </div>
        </div>
      </section>
    </main>
  )
}

function Shell({ children }: { children: ReactNode }) {
  const navigate = useNavigate()
  const [mobileOpen, setMobileOpen] = useState(false)
  const menu = (
    <nav className="space-y-2">
      {nav.map(([to, label, hint]) => (
        <NavLink key={to} to={to} onClick={() => setMobileOpen(false)} className={({ isActive }) => `block rounded-2xl px-4 py-3 transition ${isActive ? 'bg-slate-950 text-white shadow-lg shadow-slate-200' : 'text-slate-600 hover:bg-slate-100 hover:text-slate-950'}`}>
          <span className="block font-semibold">{label}</span>
          <span className="text-xs opacity-60">{hint}</span>
        </NavLink>
      ))}
    </nav>
  )
  return (
    <div className="min-h-screen">
      <aside className="fixed inset-y-0 left-0 z-30 hidden w-72 border-r border-white/80 bg-white/80 p-6 backdrop-blur-xl lg:block">
        <div className="rounded-3xl bg-gradient-to-br from-slate-950 to-teal-900 p-5 text-white shadow-xl">
          <p className="text-xs uppercase tracking-[0.28em] text-teal-200">SaaS MVP</p>
          <h1 className="mt-3 text-xl font-black">Gestion Immobilière</h1>
        </div>
        <div className="mt-8">{menu}</div>
      </aside>
      {mobileOpen && <button aria-label="Fermer le menu" className="fixed inset-0 z-40 bg-slate-950/40 lg:hidden" onClick={() => setMobileOpen(false)} />}
      <aside className={`fixed inset-y-0 left-0 z-50 w-72 bg-white p-6 shadow-2xl transition lg:hidden ${mobileOpen ? 'translate-x-0' : '-translate-x-full'}`}>
        <div className="mb-6 flex items-center justify-between">
          <strong>Menu</strong>
          <button className="rounded-xl bg-slate-100 px-3 py-2 text-sm" onClick={() => setMobileOpen(false)}>Fermer</button>
        </div>
        {menu}
      </aside>
      <header className="sticky top-0 z-20 border-b border-white/70 bg-white/75 backdrop-blur-xl lg:ml-72">
        <div className="flex items-center justify-between px-4 py-4 sm:px-8">
          <button className="rounded-2xl border border-slate-200 bg-white px-4 py-2 font-semibold lg:hidden" onClick={() => setMobileOpen(true)}>Menu</button>
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.25em] text-teal-700">Africa/Dakar</p>
            <p className="text-sm text-slate-500">Bonjour {session.fullName() ?? 'Gestionnaire'}</p>
          </div>
          <Button variant="outlined" onClick={() => { session.clear(); navigate('/login') }}>Déconnexion</Button>
        </div>
      </header>
      <main className="px-4 py-8 sm:px-8 lg:ml-72">{children}</main>
    </div>
  )
}

function PageTitle({ title, subtitle, action }: { title: string; subtitle: string; action?: ReactNode }) {
  return <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between"><div><p className="text-sm font-semibold text-teal-700">MVP Démo Client</p><h2 className="mt-1 text-3xl font-black text-slate-950 sm:text-4xl">{title}</h2><p className="mt-2 text-slate-500">{subtitle}</p></div>{action}</div>
}

function StatusBadge({ value }: { value: unknown }) {
  const text = String(value ?? '')
  return <span className={`inline-flex rounded-full px-3 py-1 text-xs font-bold ring-1 ${statusTone[text] ?? 'bg-slate-50 text-slate-600 ring-slate-200'}`}>{text}</span>
}

function EmptyState({ label }: { label: string }) {
  return <div className="rounded-3xl border border-dashed border-slate-300 bg-white/70 p-10 text-center"><p className="text-lg font-bold text-slate-800">Aucune donnée</p><p className="mt-2 text-slate-500">{label}</p></div>
}

function Dashboard() {
  const [data, setData] = useState<Record<string, number>>({})
  const [dashboardError, setDashboardError] = useState('')
  const [loading, setLoading] = useState(true)
  useEffect(() => { api<Record<string, number>>('/api/dashboard/summary').then(setData).catch(e => setDashboardError(e.message)).finally(() => setLoading(false)) }, [])
  const cards = [
    ['owners', 'Propriétaires', 'Référentiel'],
    ['properties', 'Biens', 'Patrimoine'],
    ['units', 'Unités', 'Lots gérés'],
    ['tenants', 'Locataires', 'Contacts'],
    ['activeLeases', 'Baux actifs', 'Occupation'],
    ['expectedRent', 'Loyers attendus', 'XOF'],
    ['collectedRent', 'Loyers encaissés', 'XOF'],
    ['unpaidRent', 'Impayés échus', 'À relancer'],
  ]
  return (
    <>
      <PageTitle title="Tableau de bord" subtitle="Vue synthétique de l’activité locative et financière." />
      {dashboardError && <Alert severity="error" className="mb-6">{dashboardError}</Alert>}
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {cards.map(([key, label, hint]) => <section key={key} className="rounded-3xl border border-white bg-white/85 p-6 shadow-xl shadow-slate-200/70"><p className="text-sm font-semibold text-slate-500">{label}</p>{loading ? <Skeleton height={48} /> : <p className="mt-3 text-3xl font-black text-slate-950">{key.toLowerCase().includes('rent') ? xof(data[key] ?? 0) : data[key] ?? 0}</p>}<p className="mt-3 text-xs font-semibold uppercase tracking-[0.18em] text-teal-700">{hint}</p></section>)}
      </div>
      <section className="mt-8 rounded-[2rem] bg-slate-950 p-8 text-white shadow-2xl shadow-slate-300">
        <p className="text-sm font-semibold text-teal-300">Parcours de validation</p>
        <h3 className="mt-2 text-2xl font-black">Owner → Property → Unit → Tenant → Lease → Payment → Receipt PDF</h3>
        <p className="mt-3 max-w-3xl text-slate-300">Le flux principal reste inchangé; l’interface a été clarifiée pour les essais client.</p>
      </section>
    </>
  )
}

function LookupField({ field, defaultValue, disabled }: { field: Field; defaultValue: string; disabled?: boolean }) {
  const rows = useEntities(field.lookup!.endpoint)
  const [value, setValue] = useState<Entity | null>(null)
  useEffect(() => { setValue(rows.find(row => row.id === defaultValue) ?? null) }, [rows, defaultValue])
  return (
    <>
      <input type="hidden" name={field.key} value={value?.id ?? ''} />
      <Autocomplete disabled={disabled} options={rows} value={value} onChange={(_, next) => setValue(next)} getOptionLabel={row => `${String(row[field.lookup!.labelKey] ?? '')}${field.lookup!.secondaryKey ? ` - ${String(row[field.lookup!.secondaryKey!] ?? '')}` : ''}`} renderInput={params => <TextField {...params} label={field.label} required />} />
    </>
  )
}

function ResourcePage({ title, endpoint, createEndpoint, fields, columns, allowEdit = true, allowDelete = true, allowTerminate = false }: { title: string; endpoint: string; createEndpoint?: string; fields: Field[]; columns: [string, string][]; allowEdit?: boolean; allowDelete?: boolean; allowTerminate?: boolean }) {
  const { rows, loading, error, setError, load } = usePage(endpoint)
  const [open, setOpen] = useState(false)
  const [selected, setSelected] = useState<Entity | null>(null)
  const canDelete = allowDelete && session.role() === 'ADMIN'
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const raw = Object.fromEntries(new FormData(event.currentTarget))
    const body: Record<string, unknown> = {}
    for (const field of fields) if (!(createEndpoint && field.key === 'propertyId')) body[field.key] = field.type === 'number' ? Number(raw[field.key]) : raw[field.key] || null
    try {
      let url = endpoint
      if (!selected && createEndpoint) url = createEndpoint.replace('{propertyId}', String(raw.propertyId))
      else if (selected) url = `${endpoint}/${selected.id}`
      await api(url, { method: selected ? 'PUT' : 'POST', body: JSON.stringify(body) })
      setOpen(false)
      setSelected(null)
      load()
    } catch (e) {
      setError((e as Error).message)
    }
  }
  async function terminate(row: Entity) {
    const date = prompt('Date de fin du bail (AAAA-MM-JJ)', new Date().toISOString().slice(0, 10))
    if (!date) return
    try { await api(`${endpoint}/${row.id}/terminate?date=${encodeURIComponent(date)}`, { method: 'POST' }); load() } catch (e) { setError((e as Error).message) }
  }
  return (
    <>
      <PageTitle title={title} subtitle="Gestion rapide, filtrée par organisation connectée." action={<Button variant="contained" onClick={() => { setSelected(null); setOpen(true) }}>Ajouter</Button>} />
      {error && <Alert severity="error" className="mb-6">{error}</Alert>}
      <section className="overflow-hidden rounded-[2rem] border border-white bg-white/85 shadow-xl shadow-slate-200/70">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-100">
            <thead className="bg-slate-50/80">{loading ? null : <tr>{columns.map(([, label]) => <th key={label} className="px-5 py-4 text-left text-xs font-black uppercase tracking-[0.18em] text-slate-500">{label}</th>)}<th className="px-5 py-4 text-left text-xs font-black uppercase tracking-[0.18em] text-slate-500">Actions</th></tr>}</thead>
            <tbody className="divide-y divide-slate-100">
              {loading && Array.from({ length: 4 }).map((_, index) => <tr key={index}><td className="px-5 py-4" colSpan={columns.length + 1}><Skeleton height={32} /></td></tr>)}
              {!loading && rows.map(row => <tr key={row.id} className="hover:bg-slate-50/70">{columns.map(([key]) => <td key={key} className="px-5 py-4 text-sm text-slate-700">{['status', 'type'].includes(key) ? <StatusBadge value={row[key]} /> : String(row[key] ?? '')}</td>)}<td className="space-x-2 px-5 py-4">{allowEdit && <Button size="small" onClick={() => { setSelected(row); setOpen(true) }}>Modifier</Button>}{canDelete && <Button size="small" color="error" onClick={async () => { if (confirm('Confirmer la suppression ?')) { await api(`${endpoint}/${row.id}`, { method: 'DELETE' }); load() } }}>Supprimer</Button>}{allowTerminate && row.status === 'ACTIVE' && <Button size="small" color="warning" onClick={() => terminate(row)}>Terminer</Button>}</td></tr>)}
            </tbody>
          </table>
        </div>
      </section>
      {!loading && rows.length === 0 && <div className="mt-6"><EmptyState label={`Ajoutez un premier élément dans ${title.toLowerCase()}.`} /></div>}
      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
        <form onSubmit={submit}>
          <DialogTitle>{selected ? 'Modifier' : 'Créer'} {title.toLowerCase()}</DialogTitle>
          <DialogContent><div className="mt-2 grid gap-4">{fields.map(field => field.lookup ? <LookupField key={field.key} field={field} defaultValue={String(selected?.[field.key] ?? '')} disabled={Boolean(selected && createEndpoint && field.key === 'propertyId')} /> : field.options ? <TextField key={field.key} select name={field.key} label={field.label} defaultValue={String(selected?.[field.key] ?? field.options[0])}>{field.options.map(option => <MenuItem key={option} value={option}>{option}</MenuItem>)}</TextField> : <TextField key={field.key} name={field.key} label={field.label} type={field.type ?? 'text'} defaultValue={String(selected?.[field.key] ?? '')} required={!['description', 'email', 'address', 'identityReference', 'endDate'].includes(field.key)} />)}</div></DialogContent>
          <DialogActions><Button onClick={() => setOpen(false)}>Annuler</Button><Button type="submit" variant="contained">Enregistrer</Button></DialogActions>
        </form>
      </Dialog>
    </>
  )
}

function Billing() {
  const { rows, loading, error, setError, load } = usePage('/api/rent-charges')
  const [lease, setLease] = useState<Entity | null>(null)
  const [pending, setPending] = useState<string | null>(null)
  const leases = useEntities('/api/leases')
  const visibleRows = useMemo(() => lease ? rows.filter(row => row.leaseId === lease.id) : rows, [rows, lease])
  async function pay(id: string, balance: number) {
    const value = prompt(`Montant à enregistrer (solde ${balance} XOF)`)
    if (!value) return
    setPending(id)
    try {
      await api(`/api/rent-charges/${id}/payments`, { method: 'POST', headers: { 'Idempotency-Key': crypto.randomUUID() }, body: JSON.stringify({ amount: Number(value), paymentDate: new Date().toISOString().slice(0, 10), method: 'CASH', reference: '' }) })
      load()
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setPending(null)
    }
  }
  async function reminder(id: string) { try { const result = await api<{ whatsappUrl: string }>(`/api/rent-charges/${id}/reminder-preview`, { method: 'POST' }); window.open(result.whatsappUrl, '_blank', 'noopener') } catch (e) { setError((e as Error).message) } }
  async function receipt(id: string) {
    try {
      const result = await api<{ id: string }>(`/api/rent-charges/${id}/receipt`, { method: 'POST' })
      const response = await fetch(`/api/receipts/${result.id}/pdf`, { headers: { Authorization: `Bearer ${session.token()}` } })
      if (!response.ok) throw new Error('Téléchargement impossible')
      const url = URL.createObjectURL(await response.blob())
      window.open(url, '_blank', 'noopener')
      setTimeout(() => URL.revokeObjectURL(url), 30_000)
    } catch (e) { setError((e as Error).message) }
  }
  return (
    <>
      <PageTitle title="Loyers et paiements" subtitle="Suivi des échéances, encaissements, relances et quittances." />
      {error && <Alert severity="error" className="mb-6">{error}</Alert>}
      <Autocomplete className="mb-5 max-w-xl" options={leases} value={lease} onChange={(_, value) => setLease(value)} getOptionLabel={option => `${String(option.startDate)} - ${xof(Number(option.monthlyRent))}`} renderInput={params => <TextField {...params} label="Filtrer par bail" />} />
      <section className="overflow-hidden rounded-[2rem] border border-white bg-white/85 shadow-xl shadow-slate-200/70">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-100">
            <thead className="bg-slate-50/80"><tr>{['Échéance', 'Dû', 'Payé', 'Solde', 'Statut', 'Actions'].map(label => <th key={label} className="px-5 py-4 text-left text-xs font-black uppercase tracking-[0.18em] text-slate-500">{label}</th>)}</tr></thead>
            <tbody className="divide-y divide-slate-100">
              {loading && Array.from({ length: 4 }).map((_, index) => <tr key={index}><td colSpan={6} className="px-5 py-4"><Skeleton height={32} /></td></tr>)}
              {!loading && visibleRows.map(row => <tr key={row.id} className="hover:bg-slate-50/70"><td className="px-5 py-4">{String(row.dueDate)}</td><td className="px-5 py-4">{xof(Number(row.amountDue))}</td><td className="px-5 py-4">{xof(Number(row.amountPaid))}</td><td className="px-5 py-4 font-bold">{xof(Number(row.balance))}</td><td className="px-5 py-4"><StatusBadge value={row.status} /></td><td className="space-x-2 px-5 py-4"><Button size="small" disabled={Number(row.balance) === 0 || pending === row.id} onClick={() => pay(row.id, Number(row.balance))}>Paiement</Button><Button size="small" disabled={Number(row.balance) === 0} onClick={() => reminder(row.id)}>WhatsApp</Button><Button size="small" disabled={Number(row.balance) !== 0} onClick={() => receipt(row.id)}>Quittance</Button></td></tr>)}
            </tbody>
          </table>
        </div>
      </section>
      {!loading && visibleRows.length === 0 && <div className="mt-6"><EmptyState label="Aucune échéance à afficher pour ce filtre." /></div>}
    </>
  )
}

const ownerLookup: Lookup = { endpoint: '/api/owners', labelKey: 'fullName', secondaryKey: 'phone' }
const propertyLookup: Lookup = { endpoint: '/api/properties', labelKey: 'name', secondaryKey: 'city' }
const unitLookup: Lookup = { endpoint: '/api/units', labelKey: 'name', secondaryKey: 'type' }
const tenantLookup: Lookup = { endpoint: '/api/tenants', labelKey: 'fullName', secondaryKey: 'phone' }

function ProtectedApp() {
  return <Shell><Routes><Route path="/" element={<Dashboard />} /><Route path="/owners" element={<ResourcePage title="Propriétaires" endpoint="/api/owners" fields={[{ key: 'fullName', label: 'Nom complet' }, { key: 'phone', label: 'Téléphone' }, { key: 'email', label: 'Email' }, { key: 'address', label: 'Adresse' }]} columns={[['fullName', 'Nom'], ['phone', 'Téléphone'], ['email', 'Email']]} />} /><Route path="/properties" element={<ResourcePage title="Biens" endpoint="/api/properties" fields={[{ key: 'ownerId', label: 'Propriétaire', lookup: ownerLookup }, { key: 'name', label: 'Nom' }, { key: 'description', label: 'Description' }, { key: 'address', label: 'Adresse' }, { key: 'city', label: 'Ville' }, { key: 'type', label: 'Type', options: ['APARTMENT_BUILDING', 'HOUSE', 'VILLA', 'COMMERCIAL', 'LAND'] }, { key: 'status', label: 'Statut', options: ['AVAILABLE', 'OCCUPIED', 'MAINTENANCE'] }]} columns={[['name', 'Nom'], ['city', 'Ville'], ['type', 'Type'], ['status', 'Statut']]} />} /><Route path="/units" element={<ResourcePage title="Unités" endpoint="/api/units" createEndpoint="/api/properties/{propertyId}/units" fields={[{ key: 'propertyId', label: 'Bien', lookup: propertyLookup }, { key: 'name', label: 'Nom' }, { key: 'type', label: 'Type', options: ['APARTMENT', 'ROOM', 'SHOP', 'OFFICE'] }, { key: 'status', label: 'Statut', options: ['AVAILABLE', 'OCCUPIED', 'MAINTENANCE'] }, { key: 'description', label: 'Description' }, { key: 'monthlyRent', label: 'Loyer mensuel', type: 'number' }]} columns={[['name', 'Nom'], ['type', 'Type'], ['status', 'Statut'], ['monthlyRent', 'Loyer XOF']]} />} /><Route path="/tenants" element={<ResourcePage title="Locataires" endpoint="/api/tenants" fields={[{ key: 'fullName', label: 'Nom complet' }, { key: 'phone', label: 'Téléphone' }, { key: 'email', label: 'Email' }, { key: 'address', label: 'Adresse' }, { key: 'identityReference', label: 'Référence identité' }]} columns={[['fullName', 'Nom'], ['phone', 'Téléphone'], ['email', 'Email']]} />} /><Route path="/leases" element={<ResourcePage title="Baux" endpoint="/api/leases" allowEdit={false} allowDelete={false} allowTerminate fields={[{ key: 'unitId', label: 'Unité', lookup: unitLookup }, { key: 'tenantId', label: 'Locataire', lookup: tenantLookup }, { key: 'startDate', label: 'Date début', type: 'date' }, { key: 'endDate', label: 'Date fin', type: 'date' }, { key: 'monthlyRent', label: 'Loyer', type: 'number' }, { key: 'depositAmount', label: 'Caution', type: 'number' }, { key: 'dueDay', label: 'Jour échéance', type: 'number' }]} columns={[['startDate', 'Début'], ['monthlyRent', 'Loyer XOF'], ['depositAmount', 'Caution'], ['status', 'Statut']]} />} /><Route path="/billing" element={<Billing />} /></Routes></Shell>
}

export default function App() {
  return <BrowserRouter><Routes><Route path="/login" element={<Login />} /><Route path="/*" element={session.token() ? <ProtectedApp /> : <Navigate to="/login" />} /></Routes></BrowserRouter>
}
