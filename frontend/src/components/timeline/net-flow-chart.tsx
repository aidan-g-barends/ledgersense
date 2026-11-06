"use client";

import { useState, type PointerEvent } from "react";
import type { TimelinePoint } from "@/lib/types";
import { formatDate, formatMoney } from "@/lib/money";

// The SVG's own coordinate system. It scales to any screen width.
const W = 720;
const H = 240;
const PAD = { top: 16, right: 8, bottom: 8, left: 8 };

// NEW: running net cash flow as a line, flagged days as red dots, hover for details
export function NetFlowChart({ points }: { points: TimelinePoint[] }) {
  const [hover, setHover] = useState<number | null>(null); // index of the day under the mouse

  if (points.length < 2) {
    return <p className="text-sm text-fg-muted">Not enough data to draw a chart yet.</p>;
  }

  const plotW = W - PAD.left - PAD.right;
  const plotH = H - PAD.top - PAD.bottom;
  const values = points.map((p) => p.runningNet);
  const min = Math.min(0, ...values); // WHY include 0: so the zero line is always on the chart
  const max = Math.max(0, ...values);
  const span = max - min || 1;        // WHY || 1: if everything is 0, avoid dividing by 0

  const x = (i: number) => PAD.left + (i / (points.length - 1)) * plotW;
  const y = (v: number) => PAD.top + (1 - (v - min) / span) * plotH; // 1 - ... because SVG's y grows DOWN

  const line = points.map((p, i) => `${i === 0 ? "M" : "L"}${x(i).toFixed(1)},${y(p.runningNet).toFixed(1)}`).join(" ");
  // The shaded area: the line, then down to the zero line, back to the start, close (Z)
  const area = `${line} L${x(points.length - 1).toFixed(1)},${y(0).toFixed(1)} L${x(0).toFixed(1)},${y(0).toFixed(1)} Z`;

  function handleMove(event: PointerEvent<SVGSVGElement>) {
    // Mouse position in screen pixels, converted into the SVG's own 0..720 units
    const rect = event.currentTarget.getBoundingClientRect();
    const viewX = ((event.clientX - rect.left) / rect.width) * W;
    const i = Math.round(((viewX - PAD.left) / plotW) * (points.length - 1));
    setHover(Math.min(points.length - 1, Math.max(0, i))); // clamp so we never go out of range
  }

  const last = points[points.length - 1];
  const active = hover !== null ? points[hover] : null;

  return (
    <div className="relative">
      <svg
        viewBox={`0 0 ${W} ${H}`}
        className="h-auto w-full touch-pan-y"
        role="img"
        aria-label={`Net cash flow from ${formatDate(points[0].date)} to ${formatDate(last.date)}, ending at ${formatMoney(last.runningNet)}`}
        onPointerMove={handleMove}
        onPointerLeave={() => setHover(null)}
      >
        {/* Zero line */}
        <line x1={PAD.left} x2={W - PAD.right} y1={y(0)} y2={y(0)} className="stroke-line-strong" strokeDasharray="4 4" vectorEffect="non-scaling-stroke" />
        <path d={area} className="fill-primary/10" />
        {/* WHY non-scaling-stroke: the line stays 2px thick however wide the chart stretches */}
        <path d={line} fill="none" className="stroke-primary" strokeWidth={2} strokeLinejoin="round" vectorEffect="non-scaling-stroke" />

        {points.map((p, i) =>
          p.flagCount > 0 ? (
            <circle key={p.date} cx={x(i)} cy={y(p.runningNet)} r={4.5} className="fill-critical stroke-surface" strokeWidth={2} />
          ) : null,
        )}

        {active && hover !== null && (
          <>
            <line x1={x(hover)} x2={x(hover)} y1={PAD.top} y2={H - PAD.bottom} className="stroke-line-strong" vectorEffect="non-scaling-stroke" />
            <circle cx={x(hover)} cy={y(active.runningNet)} r={4} className="fill-primary" />
          </>
        )}
      </svg>

      {/* Tooltip: normal HTML on top of the SVG, positioned as a % of the width */}
      {active && hover !== null && (
        <div
          className="pointer-events-none absolute top-0 z-10 w-48 rounded border border-line bg-raised p-2.5 text-xs shadow-lg"
          style={{
            left: `${(x(hover) / W) * 100}%`,
            // WHY: on the right half, show the tooltip on the LEFT of the cursor so it doesn't go off-screen
            transform: hover > points.length / 2 ? "translateX(calc(-100% - 10px))" : "translateX(10px)",
          }}
        >
          <p className="font-mono text-fg-muted">{formatDate(active.date)}</p>
          <p className="mt-1 font-mono text-sm font-semibold tabular-nums text-fg">{formatMoney(active.runningNet)}</p>
          <dl className="mt-1.5 space-y-0.5 font-mono tabular-nums">
            <div className="flex justify-between"><dt className="text-fg-muted">In</dt><dd className="text-positive">{formatMoney(active.moneyIn)}</dd></div>
            <div className="flex justify-between"><dt className="text-fg-muted">Out</dt><dd className="text-fg">{formatMoney(active.moneyOut)}</dd></div>
          </dl>
          {active.flagCount > 0 && (
            <p className="mt-1.5 text-critical">{active.flagCount} flag{active.flagCount > 1 ? "s" : ""} this day</p>
          )}
        </div>
      )}

      <div className="mt-2 flex justify-between font-mono text-[10px] text-fg-faint">
        <span>{formatDate(points[0].date)}</span>
        <span>{formatDate(last.date)}</span>
      </div>
    </div>
  );
}