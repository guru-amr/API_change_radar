import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login } from '../api';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const { saveToken } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const res = await login(form);
      saveToken(res.data.token);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid credentials');
    }
  };

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h2 style={styles.title}>🔭 API Change Radar</h2>
        <h3 style={styles.sub}>Sign In</h3>
        {error && <p style={styles.error}>{error}</p>}
        <form onSubmit={handleSubmit} style={styles.form}>
          <input style={styles.input} type="email" placeholder="Email" value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })} required />
          <input style={styles.input} type="password" placeholder="Password" value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })} required />
          <button style={styles.btn} type="submit">Login</button>
        </form>
        <p style={styles.footer}>No account? <Link to="/register" style={styles.link}>Register</Link></p>
      </div>
    </div>
  );
}

const styles = {
  page: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#1e1e2e' },
  card: { background: '#313244', padding: '40px', borderRadius: '12px', width: '360px', boxShadow: '0 4px 24px #0008' },
  title: { color: '#a6e3a1', textAlign: 'center', marginBottom: '4px' },
  sub: { color: '#cdd6f4', textAlign: 'center', marginBottom: '24px', fontWeight: 'normal' },
  form: { display: 'flex', flexDirection: 'column', gap: '14px' },
  input: { padding: '10px 14px', borderRadius: '8px', border: '1px solid #45475a', background: '#1e1e2e', color: '#cdd6f4', fontSize: '0.95rem' },
  btn: { padding: '10px', background: '#89b4fa', color: '#1e1e2e', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer', fontSize: '1rem' },
  error: { color: '#f38ba8', textAlign: 'center', marginBottom: '8px' },
  footer: { color: '#6c7086', textAlign: 'center', marginTop: '16px' },
  link: { color: '#89b4fa' },
};
