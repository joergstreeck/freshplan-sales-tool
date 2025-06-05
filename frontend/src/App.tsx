import { useState } from 'react';
import reactLogo from './assets/react.svg';
import viteLogo from '/vite.svg';
import './App.css';
import { ApiService } from './services/api';
import { useAuth } from './contexts/AuthContext';

function App() {
  const [count, setCount] = useState(0);
  const [pingResult, setPingResult] = useState<string>('');
  const { token } = useAuth();

  const handlePing = async () => {
    try {
      const result = await ApiService.ping(token || 'test-token');
      setPingResult(JSON.stringify(result, null, 2));
    } catch (error) {
      setPingResult(`Error: ${error}`);
    }
  };

  return (
    <>
      <div>
        <a href="https://vite.dev" target="_blank">
          <img src={viteLogo} className="logo" alt="Vite logo" />
        </a>
        <a href="https://react.dev" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
      </div>
      <h1>FreshPlan 2.0</h1>
      <div className="card">
        <button onClick={() => setCount((count) => count + 1)}>
          count is {count}
        </button>
        <button onClick={handlePing} style={{ marginLeft: '10px' }}>
          Ping API
        </button>
        {pingResult && (
          <pre style={{ textAlign: 'left', marginTop: '20px' }}>
            {pingResult}
          </pre>
        )}
      </div>
      <p className="read-the-docs">Sprint 0 - Walking Skeleton</p>
    </>
  );
}

export default App;
