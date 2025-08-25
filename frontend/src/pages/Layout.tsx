import React from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function Layout(){
  const { token, logout } = useAuth();
  const nav = useNavigate();
  const linkStyle = ({isActive}:{isActive:boolean})=>({textDecoration:'none',fontWeight:isActive?'bold':'normal',color:isActive?'#2563eb':'#222'});
  return (
    <div style={{fontFamily:'sans-serif'}}>
      <header style={{background:'#fff',borderBottom:'1px solid #ddd',padding:'8px 16px',display:'flex',gap:24,alignItems:'center'}}>
        <NavLink to="/" style={linkStyle}>메인</NavLink>
        {!token && <NavLink to="/login" style={linkStyle}>로그인</NavLink>}
        {!token && <NavLink to="/signup" style={linkStyle}>회원가입</NavLink>}
        {token && <button onClick={()=>{logout(); nav('/');}} style={{marginLeft:'auto'}}>로그아웃</button>}
      </header>
      <main style={{maxWidth:600,margin:'48px auto',background:'#f4fafc',padding:32,borderRadius:4,minHeight:300}}>
        <Outlet />
      </main>
    </div>
  );
}
