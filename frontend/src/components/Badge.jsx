const colors = {
  BREAKING: { bg: '#f38ba8', color: '#1e1e2e' },
  NON_BREAKING: { bg: '#a6e3a1', color: '#1e1e2e' },
  DEPRECATION: { bg: '#fab387', color: '#1e1e2e' },
};

export default function Badge({ label }) {
  const style = colors[label] || { bg: '#6c7086', color: '#fff' };
  return (
    <span style={{ background: style.bg, color: style.color, padding: '2px 10px', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 'bold' }}>
      {label}
    </span>
  );
}
