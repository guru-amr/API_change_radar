import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getApis, createApi, updateApi, deleteApi } from '../api';
import Navbar from '../components/Navbar';

const empty = { name: '', specUrl: '', checkIntervalSeconds: 60, active: true };

export default function Dashboard() {
  const [apis, setApis] = useState([]);
  const [form, setForm] = useState(empty);
  const [editing, setEditing] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const load = async () => {
    try { const r = await getApis(); setApis(r.data); }
    catch { setError('Failed to load APIs'); }
  };

  useEffect(() => { load(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault(); setError('');
    try {
      if (editing) { await updateApi(editing, form); }
      else { await createApi(form); }
      setForm(empty); setEditing(null); setShowForm(false); load();
    } catch (err) { setError(err.response?.data?.message || 'Error saving API'); }
  };

  const handleEdit = (api) => {
    setForm({ name: api.name, specUrl: api.specUrl, checkIntervalSeconds: api.checkIntervalSeconds, active: api.active });
    setEditing(api.id); setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this API?')) return;
    await deleteApi(id); load();
  };

  const handleCancel = () => { setForm(empty); setEditing(null); setShowForm(false); setError(''); };

  return (
    <div style={styles.page}>
      <Navbar />
      <div style={styles.container}>
        <div style={styles.header}>
          <h2 style={styles.title}>Monitored APIs</h2>
          <button style={styles.addBtn} onClick={() => { setShowForm(true); setEditing(null); setForm(empty); }}>+ Add API</button>
        </div>

        {showForm && (
          <div style={styles.formCard}>
            <h3 style={styles.formTitle}>{editing ? 'Edit API' : 'Add New API'}</h3>
            {error && <p style={styles.error}>{error}</p>}
            <form onSubmit={handleSubmit} style={styles.form}>
              <input style={styles.input} placeholder="API Name" value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })} required />
              <input style={styles.input} placeholder="Spec URL (OpenAPI/Swagger)" value={form.specUrl}
                onChange={(e) => setForm({ ...form, specUrl: e.target.value })} required />
              <input style={styles.input} type="number" placeholder="Check Interval (seconds, min 30)" value={form.checkIntervalSeconds}
                onChange={(e) => setForm({ ...form, checkIntervalSeconds: parseInt(e.target.value) })} min={30} required />
              <label style={styles.checkLabel}>
                <input type="checkbox" checked={form.active} onChange={(e) => setForm({ ...form, active: e.target.checked })} />
                &nbsp; Active
              </label>
              <div style={styles.formBtns}>
                <button style={styles.saveBtn} type="submit">{editing ? 'Update' : 'Create'}</button>
                <button style={styles.cancelBtn} type="button" onClick={handleCancel}>Cancel</button>
              </div>
            </form>
          </div>
        )}

        {apis.length === 0 && !showForm && (
          <div style={styles.empty}>No APIs monitored yet. Click "+ Add API" to get started.</div>
        )}

        <div style={styles.grid}>
          {apis.map((api) => (
            <div key={api.id} style={styles.card}>
              <div style={styles.cardHeader}>
                <span style={styles.apiName}>{api.name}</span>
                <span style={{ ...styles.statusBadge, background: api.active ? '#a6e3a1' : '#6c7086' }}>
                  {api.active ? 'Active' : 'Inactive'}
                </span>
              </div>
              <p style={styles.url}>{api.specUrl}</p>
              <div style={styles.meta}>
                <span>⏱ Every {api.checkIntervalSeconds}s</span>
                <span>🕐 {api.lastCheckedAt ? new Date(api.lastCheckedAt).toLocaleString() : 'Never checked'}</span>
              </div>
              {api.lastChangedAt && (
                <p style={styles.changed}>⚠️ Last change: {new Date(api.lastChangedAt).toLocaleString()}</p>
              )}
              <div style={styles.cardBtns}>
                <button style={styles.viewBtn} onClick={() => navigate(`/apis/${api.id}`)}>View Changes</button>
                <button style={styles.editBtn} onClick={() => handleEdit(api)}>Edit</button>
                <button style={styles.deleteBtn} onClick={() => handleDelete(api.id)}>Delete</button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

const styles = {
  page: { minHeight: '100vh', background: '#1e1e2e', color: '#cdd6f4' },
  container: { maxWidth: '1100px', margin: '0 auto', padding: '32px 24px' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' },
  title: { color: '#cdd6f4', margin: 0 },
  addBtn: { background: '#89b4fa', color: '#1e1e2e', border: 'none', padding: '10px 20px', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' },
  formCard: { background: '#313244', borderRadius: '12px', padding: '24px', marginBottom: '28px' },
  formTitle: { color: '#cdd6f4', marginTop: 0 },
  form: { display: 'flex', flexDirection: 'column', gap: '12px' },
  input: { padding: '10px 14px', borderRadius: '8px', border: '1px solid #45475a', background: '#1e1e2e', color: '#cdd6f4', fontSize: '0.95rem' },
  checkLabel: { color: '#cdd6f4', display: 'flex', alignItems: 'center' },
  formBtns: { display: 'flex', gap: '12px' },
  saveBtn: { background: '#a6e3a1', color: '#1e1e2e', border: 'none', padding: '10px 24px', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' },
  cancelBtn: { background: '#45475a', color: '#cdd6f4', border: 'none', padding: '10px 24px', borderRadius: '8px', cursor: 'pointer' },
  error: { color: '#f38ba8' },
  empty: { color: '#6c7086', textAlign: 'center', marginTop: '60px', fontSize: '1.1rem' },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '20px' },
  card: { background: '#313244', borderRadius: '12px', padding: '20px', display: 'flex', flexDirection: 'column', gap: '10px' },
  cardHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  apiName: { fontWeight: 'bold', fontSize: '1.05rem', color: '#cdd6f4' },
  statusBadge: { padding: '2px 10px', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 'bold', color: '#1e1e2e' },
  url: { color: '#89b4fa', fontSize: '0.82rem', wordBreak: 'break-all', margin: 0 },
  meta: { display: 'flex', flexDirection: 'column', gap: '4px', color: '#6c7086', fontSize: '0.82rem' },
  changed: { color: '#fab387', fontSize: '0.82rem', margin: 0 },
  cardBtns: { display: 'flex', gap: '8px', marginTop: '8px' },
  viewBtn: { background: '#89b4fa', color: '#1e1e2e', border: 'none', padding: '6px 14px', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold', flex: 1 },
  editBtn: { background: '#f9e2af', color: '#1e1e2e', border: 'none', padding: '6px 14px', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold' },
  deleteBtn: { background: '#f38ba8', color: '#1e1e2e', border: 'none', padding: '6px 14px', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold' },
};
