export type ToastType = 'info' | 'success' | 'warning' | 'error';

export function toast(message: string, type: ToastType = 'info'): void {
  const el = document.createElement('div');
  el.className = `fp-toast fp-${type}`;
  el.textContent = message;
  Object.assign(el.style, {
    position: 'fixed',
    bottom: '24px',
    right: '24px',
    padding: '12px 16px',
    borderRadius: '8px',
    fontSize: '14px',
    background: type === 'success' ? '#27ae60'
              : type === 'warning' ? '#f39c12'
              : type === 'error'   ? '#c0392b'
              : '#2d3436',
    color: '#fff',
    boxShadow: '0 4px 10px rgba(0,0,0,.15)',
    zIndex: 9999,
    opacity: '0',
    transition: 'opacity .2s ease'
  });
  document.body.appendChild(el);
  requestAnimationFrame(() => (el.style.opacity = '1'));
  setTimeout(() => {
    el.style.opacity = '0';
    el.addEventListener('transitionend', () => el.remove());
  }, 3000);
}

// Make toast available globally for testing
if (typeof window !== 'undefined') {
  (window as any).toast = toast;
}