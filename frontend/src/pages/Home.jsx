import React, { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { api, money } from "../api.js";

const SIZES = ["S", "M", "L", "XL"];
const COLORS = ["Trắng", "Đen", "Xám", "Xanh navy", "Kem", "Xanh nhạt", "Xanh đậm", "Be", "Xanh rêu"];

// UC03 xem danh sách, UC05 tìm theo từ khóa, UC06 lọc kết hợp + sắp xếp (FR06, FR08–FR11)
export default function Home() {
  const [params] = useSearchParams();
  const [cats, setCats] = useState([]);
  const [f, setF] = useState({ categoryId: "", size: "", color: "", minPrice: "", maxPrice: "", sort: "" });
  const [items, setItems] = useState(null);
  const q = params.get("q") || "";

  useEffect(() => { api("/categories").then(setCats); }, []);
  useEffect(() => {
    setItems(null);
    api("/products", { params: { ...f, q } }).then(setItems).catch(() => setItems([]));
  }, [f, q]);
  const set = (k, v) => setF((s) => ({ ...s, [k]: s[k] === v ? "" : v }));

  return (
    <div className="layout">
      <aside className="panel filters">
        <h2>Bộ lọc</h2>
        <label>Danh mục</label>
        <select value={f.categoryId} onChange={(e) => setF({ ...f, categoryId: e.target.value })}>
          <option value="">Tất cả</option>
          {cats.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <label>Size</label>
        <div className="chips">{SIZES.map((s) => <button key={s} className={"chip " + (f.size === s ? "on" : "")} onClick={() => set("size", s)}>{s}</button>)}</div>
        <label>Màu</label>
        <div className="chips">{COLORS.map((c) => <button key={c} className={"chip " + (f.color === c ? "on" : "")} onClick={() => set("color", c)}>{c}</button>)}</div>
        <label>Khoảng giá (đ)</label>
        <div className="inline">
          <input placeholder="từ" value={f.minPrice} onChange={(e) => setF({ ...f, minPrice: e.target.value.replace(/\D/g, "") })} />
          <input placeholder="đến" value={f.maxPrice} onChange={(e) => setF({ ...f, maxPrice: e.target.value.replace(/\D/g, "") })} />
        </div>
        <label>Sắp xếp</label>
        <select value={f.sort} onChange={(e) => setF({ ...f, sort: e.target.value })}>
          <option value="">Mới nhất</option><option value="price_asc">Giá tăng dần</option><option value="price_desc">Giá giảm dần</option>
        </select>
        <p><button className="btn light" style={{ width: "100%", marginTop: 16 }} onClick={() => setF({ categoryId: "", size: "", color: "", minPrice: "", maxPrice: "", sort: "" })}>Xóa bộ lọc</button></p>
      </aside>
      <section>
        <h1>{q ? `Kết quả cho "${q}"` : "Tất cả sản phẩm"} {items && <span className="muted">· {items.length} sản phẩm</span>}</h1>
        {items === null && <p className="muted">Đang tải...</p>}
        {items && items.length === 0 && <div className="panel">Không tìm thấy sản phẩm phù hợp. Hãy thử bỏ bớt bộ lọc hoặc đổi từ khóa.</div>}
        <div className="grid">
          {items && items.map((p) => (
            <Link key={p.id} to={"/san-pham/" + p.id} className="card">
              <img src={p.image} alt={p.name} />
              <div className="info">
                <div className="name">{p.name}</div>
                <div className="price">{money(p.price)}</div>
                <div className="muted">{p.sizes.length ? "Size còn: " + p.sizes.join(" · ") : "Hết hàng"} · {p.colors.join(", ")}</div>
              </div>
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
}
