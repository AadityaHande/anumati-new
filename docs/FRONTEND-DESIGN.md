# Anumati Frontend Design System

## Visual direction

Anumati uses one professional regulatory-tech visual system across the marketing site, applicant workspace, officer workspace and administration screens.

- Deep navy and cool white foundation
- Blue to teal gradient accent reserved for primary actions and brand moments
- Crisp cards, restrained borders and low-noise shadows
- Strong typographic hierarchy with compact metadata
- Industrial landscape imagery used only where it adds context
- No fake testimonials, counters, ratings, customer logos or invented outcome metrics
- No decorative cursor effects or continuous scroll animation

## Motion

Framer Motion is used for short reveal transitions and staggered content entry only. Animations are one-shot, subtle and support hierarchy. `prefers-reduced-motion` is respected globally.

## Hero asset

`frontend/public/hero-industrial.jpg` is a local visual asset used by the public hero. It is not used as a source of factual claims or metrics.

## Product language

The interface uses four consistent lifecycle verbs:

**Understand → Prepare → Execute → Continue**

The public product message is sector-agnostic. Textile is never presented as a platform boundary.

## Workspace navigation

The authenticated shell uses a fixed desktop rail with an explicit collapse control and a mobile open/close state. Navigation follows the actual route families so parent items do not remain highlighted on unrelated pages.

The business switcher uses a consistent in-app menu instead of the browser-native select control. Buttons and links use directional arrows only when they perform the corresponding navigation or action; non-navigational rows do not display misleading chevrons.

See `docs/PROJECT-WALKTHROUGH.md` for the complete frontend route map.
