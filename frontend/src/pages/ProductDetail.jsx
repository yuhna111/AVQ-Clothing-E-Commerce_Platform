import React, { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api, money } from "../api.js";
import { useApp } from "../state.jsx";

// UC04 xem chi tiết, UC20 gợi ý size, UC07 thêm vào giỏ (DT-02)
export default function ProductDetail() {
  const { id } = useParams();
  const nav = useNavigate();
  const { notify, refreshCart, session } = useApp();
  const [p, setP] = useState(null);
  const [img, setImg] = useState(0);
  const [color, setColor] = useState(null);
  const [size, setSize] = useState(null);
  const [qty, setQty] = useState(1);
  const [body, setBody] = useState({ heightCm: 170, weightKg: 62, fitPref: "VUA" });
  const [advice, setAdvice] = useState(null);

  useEffect(() => { api("/products/" + id).then((d) => { setP(d); setColor(d.variants[0]?.color); }); }, [id]);
  const colors = useMemo(() => (p ? [...new Set(p.variants.map((v) => v.color))] : []), [p]);
  const variant = p && p.variants.find((v) => v.color === color && v.size === size);
  if (!p) return <p className="muted">Đang tải...</p>;

  async function askSize() {
    try {
      const r = await api(`/products/${p.id}/size-advice`, { method: "POST", body: { ...body, saveProfile: false } });
      setAdvice(r);
      if (r.size) setSize(r.size);                       // chọn sẵn, khách vẫn đổi được (HL-01)
    } catch (e) { notify(e.message, "err"); }
  }
  async function addToCart() {
    if (!session) { nav("/dang-nhap?next=/san-pham/" + p.id); return; }    // BR03
    if (!variant) { notify("Vui lòng chọn size và màu", "err"); return; }  // BR04
    try {
      await api("/cart/items", { method: "POST", body: { variantId: variant.id, quantity: qty } });
      notify("Đã thêm vào giỏ: " + p.name + " · " + size + " / " + color);
      refreshCart();
    } catch (e) { notify(e.message, "err"); }                              // EX-02, BR05
  }

  return (
    <div className="detail">
      <div className="gallery">
        <img src={p.images[img] || p.images[0]} alt={p.name} />
        <div className="thumbs">{p.images.map((u, i) => <img key={u} src={u} onClick={() => setImg(i)} />)}</div>
      </div>
      <div>
        <h1>{p.name}</h1>
        <div className="muted">{p.category} · Chất liệu: {p.material} · Phom: {p.fitType}</div>
        <p className="price" style={{ fontSize: 24 }}>{money(p.price)}</p>
        <p>{p.description}</p>
        <b>Màu:</b>
        <div className="chips" style={{ margin: "6px 0 12px" }}>
          {colors.map((c, i) => <button key={c} className={"chip " + (c === color ? "on" : "")} onClick={() => { setColor(c); setImg(Math.min(i, p.images.length - 1)); }}>{c}</button>)}
        </div>
        <b>Size:</b>
        <div className="chips" style={{ margin: "6px 0" }}>
          {p.variants.filter((v) => v.color === color).map((v) => (
            <button key={v.id} disabled={v.stock === 0} className={"chip " + (v.size === size ? "on" : "")} onClick={() => setSize(v.size)}>{v.size}</button>
          ))}
        </div>
        {p.hasSizeChart && (
          <div className="advisor">
            <b>Gợi ý size cho bạn</b>
            <div className="row" style={{ marginTop: 8 }}>
              <div><div className="muted">Chiều cao (cm)</div><input value={body.heightCm} onChange={(e) => setBody({ ...body, heightCm: +e.target.value || 0 })} /></div>
              <div><div className="muted">Cân nặng (kg)</div><input value={body.weightKg} onChange={(e) => setBody({ ...body, weightKg: +e.target.value || 0 })} /></div>
              <div><div className="muted">Kiểu mặc</div>
                <select value={body.fitPref} onChange={(e) => setBody({ ...body, fitPref: e.target.value })}>
                  <option value="OM">Ôm</option><option value="VUA">Vừa</option><option value="RONG">Rộng</option>
                </select></div>
              <button className="btn" onClick={askSize}>Gợi ý</button>
            </div>
            {advice && (
              <p style={{ marginBottom: 0 }}>
                {advice.size ? <>Size phù hợp: <b>{advice.size}</b> {advice.confidence != null && `(độ tin cậy ${Math.round(advice.confidence * 100)}%)`} — <i>{advice.reason}</i></> : advice.reason}
              </p>
            )}
          </div>
        )}
        <div className="inline" style={{ margin: "14px 0" }}>
          <div className="qty">
            <button onClick={() => setQty(Math.max(1, qty - 1))}>−</button><span>{qty}</span><button onClick={() => setQty(Math.min(10, qty + 1))}>+</button>
          </div>
          <span className="muted">{variant ? (variant.stock > 0 ? `Còn ${variant.stock} sản phẩm` : "Hết hàng") : "Chọn size để xem tồn kho"}</span>
        </div>
        <button className="btn" style={{ width: "100%", padding: 14 }} disabled={variant && variant.stock === 0} onClick={addToCart}>THÊM VÀO GIỎ</button>
      </div>
    </div>
  );
}
