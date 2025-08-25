import React, { useState } from 'react';

export default function Signup(){
  const [username,setUsername]=useState('');
  const [password,setPassword]=useState('');
  const [birthDate,setBirthDate]=useState('');
  const [status,setStatus]=useState('');
  const submit=async(e:React.FormEvent)=>{e.preventDefault();
    const resp=await fetch('/api/public/auth/signup',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({username,password,birthDate})});
    setStatus(resp.ok?'가입 성공':'실패');
  };
  return (
    <form onSubmit={submit} style={{display:'flex',flexDirection:'column',gap:8}}>
      <h2 style={{marginTop:0}}>회원가입</h2>
      <input value={username} onChange={e=>setUsername(e.target.value)} placeholder="아이디" />
      <input type="password" value={password} onChange={e=>setPassword(e.target.value)} placeholder="비밀번호" />
      <input type="date" value={birthDate} onChange={e=>setBirthDate(e.target.value)} />
      <small>{status}</small>
      <button type="submit">가입</button>
    </form>
  );
}
