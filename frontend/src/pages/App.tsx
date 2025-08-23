import React, { useState, useEffect } from 'react';

export default function App() {
  const [weather, setWeather] = useState('');
  const [moods, setMoods] = useState<string[]>([]);
  const [budget, setBudget] = useState<number>(10000);
  const [lat, setLat] = useState<number | null>(null);
  const [lon, setLon] = useState<number | null>(null);
  const [token, setToken] = useState<string>('');
  const [result, setResult] = useState<any>(null);
  const [history, setHistory] = useState<any[]>([]);
  const [view, setView] = useState<'main'|'history'|'prefs'>('main');
  const [sharedTokenInput, setSharedTokenInput] = useState('');
  const [sharedView, setSharedView] = useState<any>(null);
  const fetchHistory = async () => {
    if (!token) return;
    const resp = await fetch('/api/private/history', { headers:{'Authorization':`Bearer ${token}`}});
    if (resp.ok) {
      const data = await resp.json();
      setHistory(data.content || []);
    }
  };
  useEffect(()=>{ if(view==='history') fetchHistory(); },[view]);
  const [shareToken, setShareToken] = useState<string>('');
  const [prefs, setPrefs] = useState<any>(null);
  const [likes, setLikes] = useState('');
  const [dislikes, setDislikes] = useState('');
  const [allergies, setAllergies] = useState('');
  const [dietTypes, setDietTypes] = useState('');
  const [notes, setNotes] = useState('');

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

  const fetchPrefs = async () => {
    if (!token) return;
    const resp = await fetch('/api/private/preferences', { headers: { 'Authorization': `Bearer ${token}` } });
    if (resp.ok) {
      const data = await resp.json();
      setPrefs(data);
      if (data) { setLikes(data.likes||''); setDislikes(data.dislikes||''); setAllergies(data.allergies||''); setDietTypes(data.dietTypes||''); setNotes(data.notes||''); }
    }
  };
  const savePrefs = async () => {
    const resp = await fetch('/api/private/preferences', { method:'POST', headers:{'Content-Type':'application/json','Authorization':`Bearer ${token}`}, body: JSON.stringify({likes,dislikes,allergies,dietTypes,notes})});
    if (resp.ok) { await fetchPrefs(); }
  };
  const shareLatest = async () => {
    if (!token) return;
    const resp = await fetch('/api/private/recommendations/share', { method:'POST', headers:{ 'Authorization': `Bearer ${token}` }});
    if (resp.ok) { const d = await resp.json(); setShareToken(d.token); }
  };

  return (
    <div style={{ fontFamily: 'sans-serif', padding: 24 }}>
      <h1>오늘 뭐 먹지?</h1>
      <nav style={{marginBottom:16}}>
        <button onClick={()=>setView('main')}>추천</button>
        <button onClick={()=>setView('history')}>히스토리</button>
        <button onClick={()=>setView('prefs')}>선호도</button>
      </nav>
  {view==='main' && (<section>
        <h3>조건 입력</h3>
        <div>날씨: <input value={weather} onChange={e => setWeather(e.target.value)} placeholder="맑음" /></div>
        <div>예산: <input type="number" value={budget} onChange={e => setBudget(parseInt(e.target.value, 10))} /></div>
        <div>기분:
          {moodOptions.map(m => <button key={m} onClick={() => toggleMood(m)} style={{ fontWeight: moods.includes(m) ? 'bold' : 'normal' }}>{m}</button>)}
        </div>
        <div>위치: {lat && lon ? `${lat.toFixed(4)}, ${lon.toFixed(4)}` : <button onClick={detectLocation}>위치 가져오기</button>}</div>
        <button onClick={recommend} disabled={!lat || !lon}>추천 받기</button>
  </section>)}
  {view==='main' && result && (<section>
        <h3>추천 결과</h3>
        {result.menuRecommendations?.map((m:any) => (
          <div key={m.menuName} style={{border:'1px solid #ddd', marginBottom:8, padding:8}}>
            <strong>{m.menuName}</strong>
            <div style={{fontSize:12,color:'#555'}}>{m.reason}</div>
            <ul>
              {m.places?.map((p:any)=>(<li key={p.name}>{p.name} - {Math.round(p.distanceMeters)}m / {p.durationMinutes}분</li>))}
            </ul>
          </div>
        ))}
        <button onClick={shareLatest}>공유 링크 생성</button>
        {shareToken && <div>공유 토큰: {shareToken} (URL: /api/public/recommendations/shared/{shareToken})</div>}
  </section>)}

  {view==='prefs' && (<section style={{marginTop:32}}>
        <h3>선호도 (Preferences)</h3>
        <button onClick={fetchPrefs}>불러오기</button>
        <div>좋아하는 것(likes): <input value={likes} onChange={e=>setLikes(e.target.value)} placeholder="김치, 치즈" /></div>
        <div>싫어하는 것(dislikes): <input value={dislikes} onChange={e=>setDislikes(e.target.value)} /></div>
        <div>알레르기(allergies): <input value={allergies} onChange={e=>setAllergies(e.target.value)} /></div>
        <div>식단(dietTypes): <input value={dietTypes} onChange={e=>setDietTypes(e.target.value)} placeholder="vegan,keto" /></div>
        <div>메모(notes): <input value={notes} onChange={e=>setNotes(e.target.value)} /></div>
        <button onClick={savePrefs}>저장</button>
  </section>)}
      {view==='history' && <section>
        <h3>최근 추천 기록</h3>
        <button onClick={fetchHistory}>새로고침</button>
        <ul>
          {history.map(h=> <li key={h.id}>{h.id} - {h.weather} - {h.moods} - {h.budget} - {new Date(h.createdAt).toLocaleString()}</li>)}
        </ul>
        <h4>공유 추천 보기</h4>
        <input placeholder="share token" value={sharedTokenInput} onChange={e=>setSharedTokenInput(e.target.value)} />
        <button onClick={async ()=>{ const r= await fetch(`/api/public/recommendations/shared/${sharedTokenInput}`); if(r.ok){ setSharedView(await r.json()); }}}>불러오기</button>
        {sharedView && <pre style={{background:'#f3f3f3',padding:8}}>{JSON.stringify(sharedView,null,2)}</pre>}
      </section>}
    </div>
  );
}
