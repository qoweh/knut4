import React, { useState } from 'react';

export default function App() {
  const [weather, setWeather] = useState('');
  const [moods, setMoods] = useState<string[]>([]);
  const [budget, setBudget] = useState<number>(10000);
  const [lat, setLat] = useState<number | null>(null);
  const [lon, setLon] = useState<number | null>(null);
  const [token, setToken] = useState<string>('');
  const [result, setResult] = useState<any>(null);

  const moodOptions = ['든든', '가볍', '달달', '매콤'];

  const toggleMood = (m: string) => {
    setMoods(prev => prev.includes(m) ? prev.filter(v => v !== m) : [...prev, m]);
  };

  const detectLocation = () => {
    navigator.geolocation.getCurrentPosition(pos => {
      setLat(pos.coords.latitude);
      setLon(pos.coords.longitude);
    });
  };

  const recommend = async () => {
    if (!token) { alert('로그인 필요'); return; }
    const resp = await fetch('/api/private/recommendations', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
      body: JSON.stringify({ weather, moods, budget, latitude: lat, longitude: lon })
    });
    if (resp.ok) setResult(await resp.json());
  };

  return (
    <div style={{ fontFamily: 'sans-serif', padding: 24 }}>
      <h1>오늘 뭐 먹지?</h1>
      <section>
        <h3>조건 입력</h3>
        <div>날씨: <input value={weather} onChange={e => setWeather(e.target.value)} placeholder="맑음" /></div>
        <div>예산: <input type="number" value={budget} onChange={e => setBudget(parseInt(e.target.value, 10))} /></div>
        <div>기분:
          {moodOptions.map(m => <button key={m} onClick={() => toggleMood(m)} style={{ fontWeight: moods.includes(m) ? 'bold' : 'normal' }}>{m}</button>)}
        </div>
        <div>위치: {lat && lon ? `${lat.toFixed(4)}, ${lon.toFixed(4)}` : <button onClick={detectLocation}>위치 가져오기</button>}</div>
        <button onClick={recommend} disabled={!lat || !lon}>추천 받기</button>
      </section>
      {result && <section>
        <h3>추천 결과</h3>
        <pre style={{ background: '#f3f3f3', padding: 12 }}>{JSON.stringify(result, null, 2)}</pre>
      </section>}
    </div>
  );
}
