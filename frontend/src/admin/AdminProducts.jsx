import React, { useEffect, useRef, useState } from "react";
import { Navigate } from "react-router-dom";
import { api, money, uploadImage } from "../api.js";
import { useApp } from "../state.jsx";

const EMPTY = { categoryId: "", name: "", price: "", fitType: "REGULAR", material: "", description: "", imageUrl: "",
  variants: [{ size: "M", color: "Đen", sku: "", stock: 10 }] };

// Bỏ dấu tiếng Việt, chỉ giữ chữ/số in hoa: "Áo Thun Đen" → "AOTHUNDEN"
const code = (t) => (t || "").normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/đ/gi, "D").toUpperCase().replace(/[^A-Z0-9]/g, "");
// SKU tự sinh khi nhân viên để trống: viết tắt tên + size + màu, tối đa 40 ký tự (product_variants.sku)
const autoSku = (name, v) => [code(name).slice(0, 16) || "SP", code(v.size), code(v.color).slice(0, 10)].filter(Boolean).join("-");

// UC14 xem sản phẩm, UC15 thêm, UC16 sửa giá/ẩn hiện, UC17 cập nhật tồn kho biến thể
export default function AdminProducts() {
  const { session, notify } = useApp();
  const [list, setList] = useState([]);
  const [cats, setCats] = useState([]);
  const [stock, setStock] = useState({});
  const [price, setPrice] = useState({});
  const [form, setForm] = useState(null);
  const load = () => api("/admin/products").then(setList);
  useEffect(() => { if (session && session.role === "STAFF") { load(); api("/categories").then(setCats); } }, [session]);
  if (!session || session.role !== "STAFF") return <Navigate to="/admin" />;

  async function saveStock(v) {
    try { await api(`/admin/variants/${v.id}/stock`, { method: "PATCH", body: { stock: Number(stock[v.id]) } }); notify(`Đã cập nhật tồn kho ${v.sku}: ${v.stock} → ${stock[v.id]}`); load(); }
    catch (e) { notify(e.message, "err"); }
  }
  async function savePrice(p) {
    try { await api(`/admin/products/${p.id}`, { method: "PUT", body: { price: Number(price[p.id]) } }); notify("Đã cập nhật giá " + p.name); load(); }
    catch (e) { notify(e.message, "err"); }
  }
  async function toggle(p) { await api(`/admin/products/${p.id}`, { method: "PUT", body: { visible: !p.visible } }); load(); }
  async function create() {
    try {
      await api("/admin/products", { method: "POST", body: { ...form, categoryId: Number(form.categoryId), price: Number(form.price),
        variants: form.variants.map((v) => ({ ...v, sku: v.sku.trim() || autoSku(form.name, v), stock: Number(v.stock) })) } });
      notify("Đã thêm sản phẩm " + form.name); setForm(null); load();
    } catch (e) { notify(e.message, "err"); }
  }
  const setV = (i, k, val) => setForm({ ...form, variants: form.variants.map((v, j) => (j === i ? { ...v, [k]: val } : v)) });

  return (
    <>
      <div className="inline" style={{ justifyContent: "space-between" }}>
        <h1>Quản lý sản phẩm và tồn kho</h1>
        <button className="btn" onClick={() => setForm(form ? null : EMPTY)}>{form ? "Đóng" : "+ Thêm sản phẩm"}</button>
      </div>
      {form && (
        <div className="panel" style={{ marginBottom: 16 }}>
          <h2>Sản phẩm mới</h2>
          <div className="two">
            <div>
              <div className="field"><label>Danh mục</label><select value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
                <option value="">-- chọn --</option>{cats.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}</select></div>
              {[["name", "Tên sản phẩm"], ["price", "Giá (đ)"], ["material", "Chất liệu"], ["description", "Mô tả"]].map(([k, l]) => (
                <div className="field" key={k}><label>{l}</label><input value={form[k]} onChange={(e) => setForm({ ...form, [k]: e.target.value })} /></div>))}
              <div className="field"><label>Ảnh sản phẩm</label>
                <ImageUploader value={form.imageUrl} onChange={(url) => setForm((f) => ({ ...f, imageUrl: url }))} /></div>
              <div className="field"><label>Phom</label><select value={form.fitType} onChange={(e) => setForm({ ...form, fitType: e.target.value })}>
                <option>SLIM</option><option>REGULAR</option><option>OVERSIZE</option></select></div>
            </div>
            <div>
              <b>Biến thể (ít nhất 1 — BR17)</b>
              <div className="muted">Để trống SKU thì hệ thống tự sinh theo tên, size, màu.</div>
              {form.variants.map((v, i) => (
                <div className="inline" key={i} style={{ margin: "8px 0" }}>
                  <input style={{ width: 50 }} value={v.size} onChange={(e) => setV(i, "size", e.target.value)} placeholder="Size" />
                  <input style={{ width: 90 }} value={v.color} onChange={(e) => setV(i, "color", e.target.value)} placeholder="Màu" />
                  <input style={{ width: 120 }} value={v.sku} onChange={(e) => setV(i, "sku", e.target.value)} placeholder={autoSku(form.name, v) || "SKU"} title="Để trống để tự sinh SKU" />
                  <input style={{ width: 60 }} value={v.stock} onChange={(e) => setV(i, "stock", e.target.value)} placeholder="Tồn" />
                </div>))}
              <button className="btn light" onClick={() => setForm({ ...form, variants: [...form.variants, { size: "", color: "", sku: "", stock: 0 }] })}>+ Biến thể</button>
              <p><button className="btn" onClick={create}>Lưu sản phẩm</button></p>
            </div>
          </div>
        </div>
      )}
      {list.map((p) => (
        <div className="panel" key={p.id} style={{ marginBottom: 12, opacity: p.visible ? 1 : 0.6 }}>
          <div className="inline" style={{ justifyContent: "space-between" }}>
            <div className="inline"><img className="thumb-sm" src={p.images[0]} width="48" height="52" alt="" /><div><b>{p.name}</b><div className="muted">{p.category} · {p.fitType}{!p.visible && " · ĐANG ẨN"}</div></div></div>
            <div className="inline">
              <input style={{ width: 110 }} placeholder={String(p.price)} value={price[p.id] ?? ""} onChange={(e) => setPrice({ ...price, [p.id]: e.target.value })} />
              <button className="btn light" disabled={!price[p.id]} onClick={() => savePrice(p)}>Sửa giá ({money(p.price)})</button>
              <button className="btn light" onClick={() => toggle(p)}>{p.visible ? "Ẩn" : "Hiện"}</button>
            </div>
          </div>
          <table style={{ marginTop: 10 }}>
            <thead><tr><th>SKU</th><th>Size</th><th>Màu</th><th>Tồn kho</th><th>Cập nhật</th></tr></thead>
            <tbody>{p.variants.map((v) => (
              <tr key={v.id}><td>{v.sku}</td><td>{v.size}</td><td>{v.color}</td><td><b style={{ color: v.stock === 0 ? "#c0392b" : "" }}>{v.stock}</b></td>
                <td className="inline"><input style={{ width: 70 }} value={stock[v.id] ?? ""} placeholder="mới" onChange={(e) => setStock({ ...stock, [v.id]: e.target.value })} />
                  <button className="btn light" disabled={stock[v.id] === undefined || stock[v.id] === ""} onClick={() => saveStock(v)}>Lưu</button></td></tr>))}
            </tbody>
          </table>
        </div>
      ))}
    </>
  );
}

const ACCEPT = ["image/jpeg", "image/png", "image/webp", "image/gif"];

// Chọn ảnh từ thiết bị (bấm hoặc kéo-thả), xem trước, tải lên server; vẫn cho dán URL ảnh có sẵn.
function ImageUploader({ value, onChange }) {
  const { notify } = useApp();
  const input = useRef(null);
  const [busy, setBusy] = useState(false);
  const [drag, setDrag] = useState(false);

  async function handle(file) {
    if (!file) return;
    if (!ACCEPT.includes(file.type)) return notify("Chỉ nhận ảnh JPG, PNG, WEBP hoặc GIF", "err");
    if (file.size > 5 * 1024 * 1024) return notify("Ảnh vượt quá 5 MB", "err");
    setBusy(true);
    try { const { url } = await uploadImage(file); onChange(url); notify("Đã tải ảnh lên"); }
    catch (e) { notify(e.message, "err"); }
    finally { setBusy(false); if (input.current) input.current.value = ""; }
  }

  return (
    <>
      <div className={"dropzone" + (drag ? " drag" : "") + (value ? " has-img" : "")} role="button" tabIndex={0}
        onClick={() => !busy && input.current.click()}
        onKeyDown={(e) => (e.key === "Enter" || e.key === " ") && input.current.click()}
        onDragOver={(e) => { e.preventDefault(); setDrag(true); }} onDragLeave={() => setDrag(false)}
        onDrop={(e) => { e.preventDefault(); setDrag(false); handle(e.dataTransfer.files[0]); }}>
        {value ? <img src={value} alt="Ảnh sản phẩm" /> : (
          <div className="muted">{busy ? "Đang tải lên…" : <>Bấm để chọn ảnh hoặc kéo-thả vào đây<br />JPG, PNG, WEBP, GIF · tối đa 5 MB</>}</div>)}
        {value && busy && <div className="dropzone-busy">Đang tải lên…</div>}
      </div>
      <input ref={input} type="file" accept={ACCEPT.join(",")} hidden onChange={(e) => handle(e.target.files[0])} />
      <div className="inline" style={{ marginTop: 6 }}>
        <input style={{ flex: 1 }} placeholder="hoặc dán URL ảnh" value={value} onChange={(e) => onChange(e.target.value)} />
        {value && <button type="button" className="btn light" onClick={() => onChange("")}>Xóa ảnh</button>}
      </div>
    </>
  );
}
