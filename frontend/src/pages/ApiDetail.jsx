import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getApi, getChanges, diffVersions } from '../api';
import Navbar from '../components/Navbar';
import Badge from '../components/Badge';

export default function ApiDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [api, setApi] = useState(null);
  const [reports, setReports] = useState([]);
  const [selected, setSelected] = useState(null);
  const [diffForm, setDiffForm] = useState({ fromVersion: '', toVersion: '' });
  const [diffResult, setDiffResult] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getApi(id).then((r) => setApi(r.data)).catch(() => navigate('/dashboard'));
    getChanges(id).then((r) => setReports(r.data)).catch(() => {});
  }, [id]);

  const handleDiff = async (e) => {
    e.preventDefault(); setError(''); setDiffResult(null);
    try {
      const r = await diffVersions(id, parseInt(diffForm.fromVersion), parseInt(diffForm.toVersion));
      setDiffResult(r.data);
    } catch (err) { setError(err.response?.data?.message || 'Diff failed'); }
  };

  const displayReport = diffResult || selected;

  return (
    <div style={styles.page}>
      <Navbar />
      <div style={styles.container}>
        <button style={styles.back} onClick={() => navigate('/dashboard')}>← Back</button>
        {api && (
          <div style={styles.apiInfo}>
            <h2 style={styles.title}>{api.name}</h2>
            <p style={styles.url}>{api.specUrl}</p>
            <div style={styles.metaRow}>
              <span>⏱ Every {api.checkIntervalSeconds}s</span>
              <span style={{ ...styles.statusBadge, background: api.active ? '#a6e3a1' : '#6c7086' }}>
                {api.active ? 'Active' : 'Inactive'}
              </span>
            </div>
          </div>
        )}

        <div style={styles.layout}>
          {/* Left: change history list */}
          <div style={styles.sidebar}>
            <h3 style={styles.sectionTitle}>Change History</h3>
            {reports.length === 0 && <p style={styles.empty}>No changes detected yet.</p>}
            {reports.map((r) => (
              <div key={r.id} style={{ ...styles.reportCard, border: selected?.id === r.id ? '1px solid #89b4fa' : '1px solid #45475a' }}
                onClick={() => { setSelected(r); setDiffResult(null); }}>
                <div style={styles.reportHeader}>
                  <span style={styles.versions}>v{r.fromVersion} → v{r.toVersion}</span>
                  {r.hasBreakingChanges && <span style={styles.breakingTag}>BREAKING</span>}
                </div>
                <div style={styles.counts}>
                  <span style={{ color: '#f38ba8' }}>💥 {r.breakingCount}</span>
                  <span style={{ color: '#a6e3a1' }}>✅ {r.nonBreakingCount}</span>
                  <span style={{ color: '#fab387' }}>⚠️ {r.deprecationCount}</span>
                </div>
                <p style={styles.reportDate}>{new Date(r.createdAt).toLocaleString()}</p>
              </div>
            ))}
          </div>

          {/* Right: diff tool + report detail */}
          <div style={styles.main}>
            <div style={styles.diffCard}>
              <h3 style={styles.sectionTitle}>Manual Diff</h3>
              {error && <p style={styles.error}>{error}</p>}
              <form onSubmit={handleDiff} style={styles.diffForm}>
                <input style={styles.input} type="number" placeholder="From Version" value={diffForm.fromVersion}
                  onChange={(e) => setDiffForm({ ...diffForm, fromVersion: e.target.value })} required min={1} />
                <input style={styles.input} type="number" placeholder="To Version" value={diffForm.toVersion}
                  onChange={(e) => setDiffForm({ ...diffForm, toVersion: e.target.value })} required min={1} />
                <button style={styles.diffBtn} type="submit">Compare</button>
              </form>
            </div>

            {displayReport && (
              <div style={styles.reportDetail}>
                <h3 style={styles.sectionTitle}>
                  Report: v{displayReport.fromVersion} → v{displayReport.toVersion}
                </h3>
                <div style={styles.summary}>
                  <div style={styles.summaryItem}><span style={{ color: '#f38ba8', fontSize: '1.4rem' }}>{displayReport.breakingCount}</span><span>Breaking</span></div>
                  <div style={styles.summaryItem}><span style={{ color: '#a6e3a1', fontSize: '1.4rem' }}>{displayReport.nonBreakingCount}</span><span>Non-Breaking</span></div>
                  <div style={styles.summaryItem}><span style={{ color: '#fab387', fontSize: '1.4rem' }}>{displayReport.deprecationCount}</span><span>Deprecations</span></div>
                </div>
                <div style={styles.itemsList}>
                  {displayReport.items?.map((item) => (
                    <div key={item.id} style={styles.changeItem}>
                      <div style={styles.changeItemHeader}>
                        <Badge label={item.severity} />
                        <span style={styles.changeType}>{item.type?.replace(/_/g, ' ')}</span>
                      </div>
                      {item.path && <p style={styles.changePath}>{item.path}</p>}
                      <p style={styles.changeDesc}>{item.description}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

const styles = {
  page: { minHeight: '100vh', background: '#1e1e2e', color: '#cdd6f4' },
  container: { maxWidth: '1200px', margin: '0 auto', padding: '24px' },
  back: { background: 'none', border: 'none', color: '#89b4fa', cursor: 'pointer', fontSize: '0.95rem', marginBottom: '16px', padding: 0 },
  apiInfo: { background: '#313244', borderRadius: '12px', padding: '20px', marginBottom: '24px' },
  title: { margin: '0 0 6px', color: '#cdd6f4' },
  url: { color: '#89b4fa', fontSize: '0.85rem', margin: '0 0 10px', wordBreak: 'break-all' },
  metaRow: { display: 'flex', gap: '16px', alignItems: 'center', color: '#6c7086', fontSize: '0.85rem' },
  statusBadge: { padding: '2px 10px', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 'bold', color: '#1e1e2e' },
  layout: { display: 'grid', gridTemplateColumns: '280px 1fr', gap: '24px' },
  sidebar: { display: 'flex', flexDirection: 'column', gap: '10px' },
  sectionTitle: { color: '#cdd6f4', marginTop: 0, marginBottom: '12px' },
  empty: { color: '#6c7086', fontSize: '0.9rem' },
  reportCard: { background: '#313244', borderRadius: '10px', padding: '14px', cursor: 'pointer', display: 'flex', flexDirection: 'column', gap: '6px' },
  reportHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  versions: { fontWeight: 'bold', color: '#cdd6f4' },
  breakingTag: { background: '#f38ba8', color: '#1e1e2e', padding: '2px 8px', borderRadius: '8px', fontSize: '0.7rem', fontWeight: 'bold' },
  counts: { display: 'flex', gap: '12px', fontSize: '0.85rem' },
  reportDate: { color: '#6c7086', fontSize: '0.78rem', margin: 0 },
  main: { display: 'flex', flexDirection: 'column', gap: '20px' },
  diffCard: { background: '#313244', borderRadius: '12px', padding: '20px' },
  diffForm: { display: 'flex', gap: '12px', alignItems: 'flex-end' },
  input: { padding: '10px 14px', borderRadius: '8px', border: '1px solid #45475a', background: '#1e1e2e', color: '#cdd6f4', fontSize: '0.95rem', width: '140px' },
  diffBtn: { background: '#89b4fa', color: '#1e1e2e', border: 'none', padding: '10px 20px', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' },
  error: { color: '#f38ba8', marginBottom: '8px' },
  reportDetail: { background: '#313244', borderRadius: '12px', padding: '20px' },
  summary: { display: 'flex', gap: '24px', marginBottom: '20px' },
  summaryItem: { display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '4px', background: '#1e1e2e', padding: '12px 20px', borderRadius: '10px', minWidth: '90px' },
  itemsList: { display: 'flex', flexDirection: 'column', gap: '10px' },
  changeItem: { background: '#1e1e2e', borderRadius: '8px', padding: '12px', display: 'flex', flexDirection: 'column', gap: '4px' },
  changeItemHeader: { display: 'flex', gap: '10px', alignItems: 'center' },
  changeType: { color: '#cdd6f4', fontWeight: 'bold', fontSize: '0.9rem' },
  changePath: { color: '#89b4fa', fontSize: '0.82rem', margin: 0, fontFamily: 'monospace' },
  changeDesc: { color: '#6c7086', fontSize: '0.85rem', margin: 0 },
};
