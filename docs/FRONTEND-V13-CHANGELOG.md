# Anumati frontend V13

This release is a frontend-only visual and UX upgrade. No backend services, migrations, APIs or business logic were changed.

## Major changes

### Public site
- Rebuilt the hero around an editorial operating-model layout instead of a floating dashboard card.
- Added explicit hover/active navigation behavior.
- Reworked capability rows into asymmetric, full-width interactions with clear arrow affordances.
- Rebuilt the lifecycle section as a high-contrast sequence with direct section navigation.
- Simplified the trust section and final call to action.

### Applicant workspace
- Fixed navigation rail remains stable while content scrolls.
- Collapsed rail is a compact icon rail; mobile navigation is a drawer.
- Profile switcher uses a controlled menu rather than the browser-native select.
- Added deterministic parent navigation in the top bar for deep routes.
- Replaced gradient/soft-floating states with border, spacing and inset active states.
- Reduced excessive corner rounding and heavy shadows.

### Department workspace
- Added a dedicated responsive department shell with its own rail and top bar.
- Deep department pages now expose a clear back path to Control Tower.
- Added a compact operational metric strip instead of uniform metric cards.
- Rebuilt Control Tower around an attention queue, application table, desk summary and quick desks.
- Added persistent Applicant View switching.
- Added mobile drawer behavior and keyboard-safe interaction states.

## Navigation map

See `docs/FRONTEND-WALKTHROUGH.md` for the complete route map and interaction conventions.

## Validation completed in the packaging environment

- 40 TS/TSX source files transpile successfully with TypeScript syntax checking.
- Literal internal href audit: 0 unresolved routes.
- Icon import audit: PASS.
- Backend source was not modified in this frontend-only release.

A full Next.js production build should still be run on the Windows demo machine using the project setup scripts.
