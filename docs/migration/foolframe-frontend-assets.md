# FoolFrame Frontend Asset Coverage

Date: 2026-07-15

This inventory classifies the old Web assets under `../FoolFrame/src/Web`
before any new Vue work is added. Runtime View coverage remains authoritative
in `foolframe-view-matrix.md`.

## Inventory

| Surface | Count | Migration classification |
| --- | ---: | --- |
| Root Jade pages | 12 | 9 active shells/pages, 2 template placeholders, 1 inactive Group wrapper |
| Jade includes | 8 | 6 active partials, 2 unreferenced fragments |
| Application JavaScript modules | 23 | 21 behavior/helper modules, 2 replaced bootstrap modules |

## Root Pages

- Active shells and pages: `index.jade`, `layout.jade`, `default.jade`,
  `main.jade`, `view.jade`, `viewWithChart.jade`, `detailView.jade`,
  `item.jade`, and `Sudoku.jade`.
- `about.jade` and `contact.jade` contain only the generated Visual Studio
  placeholder copy and expose no product workflow. Their SPA fallback remains
  intentional.
- `Group.jade` is not rendered by the old route table or current top-level
  View metadata. Group panels are covered through `includes/Group.jade` inside
  the Sudoku renderer.

## Includes

- Active partials: `tbar.jade`, `List.jade`, `Group.jade`, `Item.jade`,
  `Map.jade`, and `linechart.jade`.
- `TopBar.jade` and `leftbar.jade` have no include or route reference in the
  old Web source. They are not runtime surfaces to reproduce.

## Application Modules

- Auth and shell behavior: `login.js`, `menuinfo.js`, `message.js`,
  `showerror.js`, and `operation.js` map to the Vue login, shell, menu,
  message, dialog, and operation paths.
- List, detail, and report behavior: `querylistdata.js`, `navbar.js`,
  `optionSet.js`, `detailview.js`, `setextype.js`, `savetext.js`, and
  `mkreport.js` map to the shared View panels, pagination, metadata editor,
  child-draft, operation, and report flows. `optionSet.js` only shallow-merges
  defined pagination options; reactive Vue props/state provide that boundary.
- Sudoku and visual behavior: `Sudoku.js`, `groupview.js`, `subitem.js`,
  `mapview.js`, `baidumaputil.js`, `swchartLine.js`, `viewWithChart.js`,
  `timer.js`, and `ServerUtil.js` map to the shared Sudoku, map, chart,
  auto-refresh, and success-only request paths.
- `reqconfig.js` and `swapp.js` are RequireJS path/shim configuration and
  Angular module bootstrap infrastructure. Vite and Vue replace them; they do
  not define a user interaction to reproduce.

## Current Runtime Reachability

- The imported catalog contains 118 Views: 116 use the default top-level
  renderer, one uses `Sudoku`, and one uses `viewWithChart`.
- The route matrix resolves 58 ordinary lists, one chart, one Sudoku, 57
  details, and one Map panel, with metadata/data initialization passing for all
  118 Views.
- All 927 current ViewItems resolve through their View model/property metadata;
  the 465 editable items use only editor types already handled by the shared
  Vue metadata editor.
- No current `fool_sys_view_item` row has a non-empty `format_regx`; there is
  no reachable application-specific row class awaiting a stylesheet rule.

New frontend migration work should identify a concrete source/runtime
contradiction in one of these mapped surfaces. File presence alone is not a
migration gap.
