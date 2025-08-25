import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';

interface AuthCtx {
  token: string;
  setToken: (t: string) => void;
  logout: () => void;
}

const C = createContext<AuthCtx | undefined>(undefined);

export function AuthProvider({children}:{children:ReactNode}) {
  const [token,setTokenState] = useState(()=> localStorage.getItem('token')||'');
  const setToken = (t:string)=>{ setTokenState(t); if(t) localStorage.setItem('token',t); else localStorage.removeItem('token'); };
  const logout = ()=> setToken('');
  // sync across tabs
  useEffect(()=>{ const h=(e:StorageEvent)=>{ if(e.key==='token') setTokenState(e.newValue||''); }; window.addEventListener('storage',h); return ()=>window.removeEventListener('storage',h); },[]);
  return <C.Provider value={{token,setToken,logout}}>{children}</C.Provider>;
}

export const useAuth = () => {
  const ctx = useContext(C); if(!ctx) throw new Error('AuthContext not found'); return ctx;
};