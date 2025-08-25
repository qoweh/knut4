import React,{useState} from 'react';
import { useAuth } from '../auth/AuthContext';

export default function Login(){
  const { setToken } = useAuth();
  const [username,setUsername]=useState('');
  const [password,setPassword]=useState('');
  const [status,setStatus]=useState('');
  const submit=async(e:React.FormEvent)=>{e.preventDefault();
    const resp=await fetch('/api/public/auth/login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({username,password})});
  if(resp.ok){ const d=await resp.json(); setToken(d.accessToken); setStatus('로그인 성공'); } else setStatus('실패');
  };
  return (
    <form onSubmit={submit} style={{display:'flex',flexDirection:'column',gap:8}}>
      <h2 style={{marginTop:0}}>로그인</h2>
      <input value={username} onChange={e=>setUsername(e.target.value)} placeholder="아이디" />
      <input type="password" value={password} onChange={e=>setPassword(e.target.value)} placeholder="비밀번호" />
      <small>{status}</small>
      <button type="submit">로그인</button>
    </form>
  );
}
