import React, { createContext, useCallback, useContext, useEffect, useState } from "react";
import { api, getSession } from "./api.js";

const Ctx = createContext(null);
export const useApp = () => useContext(Ctx);

export function AppProvider({ children }) {
  const [session, setSession] = useState(getSession());
  const [cartCount, setCartCount] = useState(0);
  const [toast, setToast] = useState(null);

  const notify = useCallback((msg, type = "ok") => {
    setToast({ msg, type, id: Date.now() });
    setTimeout(() => setToast(null), 3200);
  }, []);
  const login = (tok) => {
    const s = { token: tok.accessToken, role: tok.role, name: tok.fullName };
    localStorage.setItem("avq_session", JSON.stringify(s)); setSession(s);
  };
  const logout = () => { localStorage.removeItem("avq_session"); setSession(null); setCartCount(0); };
  const refreshCart = useCallback(async () => {
    if (!session || session.role !== "CUSTOMER") { setCartCount(0); return; }
    try { const c = await api("/cart"); setCartCount(c.totalQuantity); } catch { setCartCount(0); }
  }, [session]);
  useEffect(() => { refreshCart(); }, [refreshCart]);

  return (
    <Ctx.Provider value={{ session, login, logout, cartCount, setCartCount, refreshCart, notify }}>
      {children}
      {toast && <div key={toast.id} className={"toast " + toast.type}>{toast.msg}</div>}
    </Ctx.Provider>
  );
}
