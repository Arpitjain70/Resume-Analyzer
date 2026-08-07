/**
 * ─────────────────────────────────────────────────────────
 * ScoreCircle.jsx — Circular ATS Score Display
 * Location: src/components/ScoreCircle.jsx
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Reusable component to visually display a score (0-100) as a colored circle.
 * Used in the Dashboard and Analysis pages.
 *
 * COLOR CODING:
 * - 0-49:  Red    (needs significant improvement)
 * - 50-74: Yellow (average, needs work)
 * - 75-89: Blue   (good)
 * - 90+:   Green  (excellent)
 *
 * SVG CIRCLE TECHNIQUE:
 * We draw two circles. The background grey circle, then a foreground circle
 * whose "stroke-dasharray" and "stroke-dashoffset" simulate a progress arc.
 */

const ScoreCircle = ({ score = 0, size = 120, strokeWidth = 10, label = '' }) => {
  // Circle geometry
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  // How much of the circle to fill based on score
  const fillAmount = circumference - (score / 100) * circumference;

  // Color based on score
  const getColor = (s) => {
    if (s >= 90) return '#22c55e'; // green-500
    if (s >= 75) return '#3b82f6'; // blue-500
    if (s >= 50) return '#f59e0b'; // amber-500
    return '#ef4444';              // red-500
  };

  const color = getColor(score);

  return (
    <div className="flex flex-col items-center gap-2">
      {/* SVG Circle */}
      <svg width={size} height={size} style={{ transform: 'rotate(-90deg)' }}>
        {/* Background circle (grey track) */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke="#e5e7eb"
          strokeWidth={strokeWidth}
        />
        {/* Foreground circle (colored progress) */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={color}
          strokeWidth={strokeWidth}
          strokeDasharray={circumference}
          strokeDashoffset={fillAmount}
          strokeLinecap="round"
          style={{ transition: 'stroke-dashoffset 0.8s ease' }}
        />
        {/* Score text in the center — rotate back since SVG is rotated */}
        <text
          x="50%"
          y="50%"
          textAnchor="middle"
          dominantBaseline="central"
          style={{
            transform: 'rotate(90deg)',
            transformOrigin: '50% 50%',
            fontSize: size * 0.22 + 'px',
            fontWeight: '700',
            fill: color,
            fontFamily: 'Inter, sans-serif',
          }}
        >
          {score}
        </text>
      </svg>
      {/* Label below the circle */}
      {label && (
        <span className="text-sm font-medium text-gray-600 text-center">{label}</span>
      )}
    </div>
  );
};

export default ScoreCircle;
