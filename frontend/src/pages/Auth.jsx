import React, { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { api } from "../api.js";
import { useApp } from "../state.jsx";

// UC02 đăng nhập (khóa 15 phút sau 5 lần sai — BR16)
export function Login() {
  const { login, notify } = useApp();
  const nav = useNavigate(); const [p] = useSearchParams();
  const [f, setF] = useState({ identifier: "khach@avq.vn", password: "" });
  const [err, setErr] = useState("");
  async function submit(e) {
    e.preventDefault();
    try { login(await api("/auth/login", { method: "POST", body: f })); notify("Đăng nhập thành công"); nav(p.get("next") || "/"); }
    catch (e2) { setErr(e2.message); }
  }
  return (
    <form className="center-box panel" onSubmit={submit}>
      <h1>Đăng nhập</h1>
      <div className="field"><label>Email hoặc số điện thoại</label><input value={f.identifier} onChange={(e) => setF({ ...f, identifier: e.target.value })} /></div>
      <div className="field"><label>Mật khẩu</label><input type="password" value={f.password} onChange={(e) => setF({ ...f, password: e.target.value })} /></div>
      {err && <p className="err-text">{err}</p>}
      <button className="btn" style={{ width: "100%" }}>Đăng nhập</button>
      <p className="muted">Chưa có tài khoản? <Link to="/dang-ky">Đăng ký</Link> · Tài khoản demo: khach@avq.vn / matkhau123</p>
    </form>
  );
}

// UC01 đăng ký (BR01, mật khẩu ≥ 8 ký tự)
export function Register() {
  const { login, notify } = useApp();
  const nav = useNavigate();
  const [f, setF] = useState({ fullName: "", email: "", phone: "", password: "" });
  const [err, setErr] = useState("");
  async function submit(e) {
    e.preventDefault();
    try { login(await api("/auth/register", { method: "POST", body: f })); notify("Tạo tài khoản thành công"); nav("/"); }
    catch (e2) { setErr(e2.message); }
  }
  return (
    <form className="center-box panel" onSubmit={submit}>
      <h1>Đăng ký</h1>
      {[["fullName", "Họ tên"], ["email", "Email"], ["phone", "Số điện thoại"], ["password", "Mật khẩu (tối thiểu 8 ký tự)"]].map(([k, l]) => (
        <div className="field" key={k}><label>{l}</label><input type={k === "password" ? "password" : "text"} value={f[k]} onChange={(e) => setF({ ...f, [k]: e.target.value })} /></div>
      ))}
      {err && <p className="err-text">{err}</p>}
      <button className="btn" style={{ width: "100%" }}>Tạo tài khoản</button>
    </form>
  );
}
