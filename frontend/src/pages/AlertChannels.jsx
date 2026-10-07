import { useEffect, useState } from 'react';
import { getChannels, createChannel, deleteChannel } from '../api';
import Navbar from '../components/Navbar';

const TYPES = ['EMAIL', 'SLACK_WEBHOOK', 'CUSTOM_WEBHOOK'];
const placeholders = {
  EMAIL: 'you@example.com',
  SLACK_WEBHOOK: 'https://hooks.slack.com/services/...',
  CUSTOM_WEBHOOK: 'https://your-server.com/webhook',
};

export default function AlertChannels() {
  const [channels, setChannels] = useState([]);
  const [form, setForm] = useState({ type: 'EMAIL', target: '', enabled: true });
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);

  const load = async () => {
    try { const r = await getChannels(); setChannels(r.data); }
    catch { setError('Failed to load channels'); }
  };

  useEffect(() => { load(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault(); setError('');
    try {
      await createChannel(form);
      setForm({ type: 'EMAIL', target: '', enabled: true });
      setShowForm(false); load();
    } catch (err) { setError(err.response?.data?.message || 'Error creating channel'); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this alert channel?')) return;
    await deleteChannel(id); load();
  };

  const typeColors = { EMAIL: '#89b4fa', SLACK_WEBHOOK: '#a6e3a1', CUSTOM_WEBHOOK: '#fab387' };

  return (
    <div style={styles.page}>
      <Navbar />
      <div style={styles.container}>
        <div style={styles.header}>
          <h2 style={styles.title}>Alert Channels</h2>
          <button style={styles.addBtn} onClick={() => setShowForm(true)}>+ Add Channel</button>
        </div>

        {showForm && (
          <div style={styles.formCard}>
            <h3 style={styles.formTitle}>New Alert Channel</h3>
            {error && <p style={styles.error}>{error}</p>}
            <form onSubmit={handleSubmit} style={styles.form}>
              <select style={styles.input} value={form.type}
                onChange={(e) => setForm({ ...form, type: e.target.value, target: '' })}>
                {TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
              </select>
              <input style={styles.input} placeholder={placeholders[form.type]} value={form.target}
                onChange={(e) => setForm({ ...form, target: e.target.value })} required />
              <label style={styles.checkLabel}>
                <input type="checkbox" checked={form.enabled}
                  onChange={(e) => setForm({ ...form, enabled: e.target.checked })} />
                &nbsp; Enabled
              </label>
              <div style={styles.formBtns}>
                <button style={styles.saveBtn} type="submit">Create</button>
                <button style={styles.cancelBtn} type="button" onClick={() => { setShowForm(false); setError(''); }}>Cancel</button>
              </div>
            </form>
          </div>
        )}

        {channels.length === 0 && !showForm && (
          <div style={styles.empty}>No alert channels yet. Add one to get notified when APIs change.</div>
        )}

        <div style={styles.grid}>
          {channels.map((ch) => (
            <div key={ch.id} style={styles.card}>
              <div style={styles.cardHeader}>
                <span style={{ ...styles.typeBadge, background: typeColors[ch.type] || '#6c7086' }}>{ch.type}</span>
                <span style={{ ...styles.statusDot, background: ch.enabled ? '#a6e3a1' : '#6c7086' }}>
                  {ch.enabled ? 'Enabled' : 'Disabled'}
                </span>
              </div>
              <p style={styles.target}>{ch.target}</p>
              <p style={styles.date}>Added: {new Date(ch.createdAt).toLocaleString()}</p>
              <button style={styles.deleteBtn} onClick={() => handleDelete(ch.id)}>Delete</button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

const styles = {
  page: { minHeight: '100vh', background: '#1e1e2e', color: '#cdd6f4' },
  container: { maxWidth: '900px', margin: '0 auto', padding: '32px 24px' },
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
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '16px' },
  card: { background: '#313244', borderRadius: '12px', padding: '20px', display: 'flex', flexDirection: 'column', gap: '10px' },
  cardHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  typeBadge: { padding: '3px 12px', borderRadius: '12px', fontSize: '0.78rem', fontWeight: 'bold', color: '#1e1e2e' },
  statusDot: { padding: '2px 10px', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 'bold', color: '#1e1e2e' },
  target: { color: '#89b4fa', fontSize: '0.85rem', wordBreak: 'break-all', margin: 0 },
  date: { color: '#6c7086', fontSize: '0.78rem', margin: 0 },
  deleteBtn: { background: '#f38ba8', color: '#1e1e2e', border: 'none', padding: '7px', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold' },
};
