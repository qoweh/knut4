import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../auth/AuthContext';

// Lightweight Naver Maps loader (client-side only)
declare global {
  interface Window { naver: any }
}

function useNaverMaps(apiKey?: string) {
  const [ready, setReady] = useState(false);
  useEffect(()=>{
    if (typeof window === 'undefined') return;
    if ((window as any).naver?.maps) { setReady(true); return; }
    const existing = document.getElementById('naver-map-script');
    if (existing) { existing.addEventListener('load', ()=>setReady(true)); return; }
    const script = document.createElement('script');
    script.id = 'naver-map-script';
    script.src = `https://oapi.map.naver.com/openapi/v3/maps.js?ncpClientId=${apiKey||'demo'}&submodules=geocoder`; // demo placeholder
    script.async = true;
    script.onload = () => setReady(true);
    document.head.appendChild(script);
  },[apiKey]);
  return ready;
}

const MapView: React.FC<{places:any[], userLat?:number|null, userLon?:number|null}> = ({places, userLat, userLon}) => {
  const mapRef = useRef<HTMLDivElement|null>(null);
  const [mapObj, setMapObj] = useState<any>(null);
  const ready = useNaverMaps(import.meta.env.VITE_NAVER_MAP_KEY);
  useEffect(()=>{
    if (!ready || !mapRef.current || !places.length) return;
    if (!mapObj) {
      const center = new window.naver.maps.LatLng(places[0].latitude, places[0].longitude);
      const map = new window.naver.maps.Map(mapRef.current, { center, zoom: 15 });
      setMapObj(map);
      places.slice(0,5).forEach(p=> {
        const marker = new window.naver.maps.Marker({ position: new window.naver.maps.LatLng(p.latitude, p.longitude), map, title: p.name });
        const info = new window.naver.maps.InfoWindow({ content: `<div style=\"padding:4px;font-size:12px;\"><strong>${p.name}</strong><br/>${Math.round(p.distanceMeters)}m</div>`});
        window.naver.maps.Event.addListener(marker, 'click', ()=>{
          info.open(map, marker);
        });
      });
      if (userLat && userLon) {
        new window.naver.maps.Circle({
          map,
          center: new window.naver.maps.LatLng(userLat, userLon),
          radius: 5,
          strokeColor: '#2563eb', strokeWeight:2, fillColor:'#3b82f6', fillOpacity:0.5
        });
      }
    } else {
      // update markers by clearing & re-adding (simple approach)
      while ((mapRef.current as any)?.firstChild) (mapRef.current as any).removeChild((mapRef.current as any).firstChild);
      const center = new window.naver.maps.LatLng(places[0].latitude, places[0].longitude);
      mapObj.setCenter(center);
      places.slice(0,5).forEach(p=> {
        const marker = new window.naver.maps.Marker({ position: new window.naver.maps.LatLng(p.latitude, p.longitude), map: mapObj, title: p.name });
        const info = new window.naver.maps.InfoWindow({ content: `<div style=\"padding:4px;font-size:12px;\"><strong>${p.name}</strong><br/>${Math.round(p.distanceMeters)}m</div>`});
        window.naver.maps.Event.addListener(marker, 'click', ()=> info.open(mapObj, marker));
      });
      if (userLat && userLon) {
        new window.naver.maps.Circle({
          map: mapObj,
          center: new window.naver.maps.LatLng(userLat, userLon),
          radius: 5,
          strokeColor: '#2563eb', strokeWeight:2, fillColor:'#3b82f6', fillOpacity:0.5
        });
      }
    }
  },[ready, places, mapObj]);
  return <div ref={mapRef} style={{height:200,width:'100%', border:'1px solid #ccc'}}>{!ready && '지도를 불러오는 중...'}</div>;
};

export default function App() { // Recommendation Page
  // (Weather removed) No weather state needed; backend defaults internally.
  const [moods, setMoods] = useState<string[]>([]);
  // Budget represented as maximum value selected from predefined ranges
  const [budget, setBudget] = useState<number>(10000);
  const [lat, setLat] = useState<number | null>(null);
  const [lon, setLon] = useState<number | null>(null);
  const { token } = useAuth();
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
  const [useCurrentLocation, setUseCurrentLocation] = useState(true);

  const moodOptions = ['든든', '가볍', '달달', '매콤'];

  const budgetRanges = [
    { label: '~1만원', max: 10000 },
    { label: '1~2만원', max: 20000 },
    { label: '2~3만원', max: 30000 },
    { label: '3~4만원', max: 40000 },
    { label: '4만원 이상', max: 999999 }
  ];

  const styles = {
    chipGroup: { display: 'flex', flexWrap: 'wrap' as const, gap: '6px', marginTop: 4 },
    chip: (active:boolean) => ({
      padding: '6px 14px',
      borderRadius: 20,
      border: active ? '1px solid #2563eb' : '1px solid #d1d5db',
      background: active ? 'linear-gradient(90deg,#2563eb,#1d4ed8)' : '#f8fafc',
      color: active ? '#fff' : '#1e293b',
      cursor: 'pointer',
      fontSize: 13,
      lineHeight: 1.1,
      fontWeight: active ? 600 : 400,
      boxShadow: active ? '0 2px 4px rgba(37,99,235,0.35)' : '0 1px 2px rgba(0,0,0,0.08)',
      transition: 'all .15s',
      userSelect: 'none' as const
    }),
    chipMood: (active:boolean) => ({
      padding: '10px 18px',
      borderRadius: 14,
      border: active ? '2px solid #f59e0b' : '2px solid transparent',
      background: active ? 'linear-gradient(135deg,#fbbf24,#f59e0b)' : 'linear-gradient(135deg,#ffffff,#f1f5f9)',
      color: '#111827',
      cursor: 'pointer',
      fontSize: 14,
      fontWeight: 600,
      position: 'relative' as const,
      minWidth: 64,
      textAlign: 'center' as const,
      boxShadow: active ? '0 3px 6px rgba(245,158,11,0.4)' : '0 1px 3px rgba(0,0,0,0.12)',
      transition: 'transform .15s, box-shadow .15s',
    }),
    moodEmoji: { fontSize: 16, marginRight: 4 },
    sectionCard: {
      background: '#f8fbfd',
      padding: 16,
      borderRadius: 6,
      border: '1px solid #e2e8f0',
      maxWidth: 600
    }
  };

  const toggleMood = (m: string) => {
    setMoods(prev => prev.includes(m) ? prev.filter(v => v !== m) : [...prev, m]);
  };

  const detectLocation = () => {
    navigator.geolocation.getCurrentPosition(pos => {
      setLat(pos.coords.latitude); setLon(pos.coords.longitude);
    });
  };
  useEffect(()=> { if (useCurrentLocation && (lat===null || lon===null)) detectLocation(); }, [useCurrentLocation]);

  const recommend = async () => {
    if (!token) { alert('로그인 필요'); return; }
    const resp = await fetch('/api/private/recommendations', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
  body: JSON.stringify({ weather: undefined, moods, budget, latitude: lat, longitude: lon })
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
    <div style={{ fontFamily: 'sans-serif' }}>
      <h2 style={{marginTop:0}}>메뉴 추천</h2>
  {view==='main' && (
    <section style={styles.sectionCard}>
      <h3 style={{marginTop:0}}>조건 입력</h3>
  {/* Weather UI removed */}
      <div style={{marginBottom:12}}>
        <strong>예산 (최대)</strong>:
        <div style={styles.chipGroup}>
          {budgetRanges.map(r => (
            <div key={r.label} onClick={()=>setBudget(r.max)} style={styles.chip(budget===r.max)}>{r.label}</div>
          ))}
        </div>
        <div style={{fontSize:11,color:'#64748b',marginTop:4}}>선택된 최대 예산: {budget>=999999? '4만원 이상' : `${budget.toLocaleString()}원`}</div>
      </div>
      <div style={{marginBottom:12}}>
        <strong>기분</strong>:
        <div style={styles.chipGroup}>
          {moodOptions.map(m => {
            const active = moods.includes(m);
            const emoji = m==='든든'?'🍲': m==='가볍'?'🥗': m==='달달'?'🍰':'🌶️';
            return (
              <div key={m} onClick={()=>toggleMood(m)} style={styles.chipMood(active)}
                onMouseDown={e=> (e.currentTarget.style.transform='scale(.94)')}
                onMouseUp={e=> (e.currentTarget.style.transform='scale(1)')}
                onMouseLeave={e=> (e.currentTarget.style.transform='scale(1)')}>
                <span style={styles.moodEmoji}>{emoji}</span>{m}
                {active && <span style={{position:'absolute',top:4,right:6,fontSize:10,color:'#92400e'}}>선택</span>}
              </div>
            );
          })}
        </div>
        <div style={{fontSize:11,color:'#64748b',marginTop:4}}>여러 개 선택 가능</div>
      </div>
      <div style={{marginBottom:12}}>
        <label>
          <input type="checkbox" checked={useCurrentLocation} onChange={e=>setUseCurrentLocation(e.target.checked)} /> 현재 위치 사용
        </label>
        {useCurrentLocation ? (
          <span style={{marginLeft:8,fontSize:12}}>{lat && lon ? `${lat.toFixed(4)}, ${lon.toFixed(4)}` : '위치 확인 중 / 권한 필요'}</span>
        ) : (
          <span style={{marginLeft:8}}>
            위도 <input style={{width:100}} value={lat??''} onChange={e=>setLat(parseFloat(e.target.value)||0)} />
            경도 <input style={{width:100,marginLeft:4}} value={lon??''} onChange={e=>setLon(parseFloat(e.target.value)||0)} />
          </span>
        )}
      </div>
      <button onClick={recommend} disabled={!lat || !lon} style={{padding:'10px 20px',background:'#2563eb',color:'#fff',border:'none',borderRadius:6,cursor: (!lat||!lon)?'not-allowed':'pointer', boxShadow:'0 2px 4px rgba(0,0,0,0.2)'}}>
        추천 받기
      </button>
    </section>
  )}
  {view==='main' && result && (<section>
        <h3>추천 결과</h3>
        {result.menuRecommendations?.map((m:any) => (
          <div key={m.menuName} style={{border:'1px solid #ddd', marginBottom:8, padding:8}}>
            <strong>{m.menuName}</strong>
            <div style={{fontSize:12,color:'#555'}}>{m.reason}</div>
            <ul>
              {m.places?.map((p:any)=>(<li key={p.name}>{p.name} - {Math.round(p.distanceMeters)}m / {p.durationMinutes}분</li>))}
            </ul>
            {m.places && m.places.length>0 && (<MapView places={m.places} userLat={lat} userLon={lon} />)}
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
