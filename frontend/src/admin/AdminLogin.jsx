import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api.js";
import { useApp } from "../state.jsx";

// UC13 nhân viên đăng nhập
export default function AdminLogin() {
  const { login, notify } = useApp();
  const nav = useNavigate();
  const [f, setF] = useState({ identifier: "nhanvien1", password: "" });
  const [err, setErr] = useState("");
  async function submit(e) {
    e.preventDefault();
    try { login(await api("/auth/staff/login", { method: "POST", body: f })); notify("Xin chào nhân viên"); nav("/admin/don-hang"); }
    catch (e2) { setErr(e2.message); }
  }
  return (
    <form className="center-box panel" onSubmit={submit}>
      <h1>Đăng nhập quản trị</h1>
      <div className="field"><label>Tên đăng nhập</label><input value={f.identifier} onChange={(e) => setF({ ...f, identifier: e.target.value })} /></div>
      <div className="field"><label>Mật khẩu</label><input type="password" value={f.password} onChange={(e) => setF({ ...f, password: e.target.value })} /></div>
      {err && <p className="err-text">{err}</p>}
      <button className="btn" style={{ width: "100%" }}>Đăng nhập</button>
      <p className="muted">Tài khoản demo: nhanvien1 / staff@123</p>
    </form>
  );
}
