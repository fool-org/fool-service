# FoolFrame View Route Matrix

Generated from the Docker `SW_SYS_VIEW` catalog by
`python scripts/legacy_view_matrix.py --output docs/migration/foolframe-view-matrix.md`.

Classification follows `../FoolFrame/src/Web/routes/index.js`: list routes
select `view`, `viewWithChart`, or `Sudoku` from `TempFile`; object, new,
and schema routes use `detailView.jade` and `item.jade`; Map is embedded
through `Sudoku.jade` and `includes/Map.jade`.

Catalog: 118 Views (chart=1, detail=57, list=58, map-panel=1, sudoku=1). Runtime metadata/data: 118/118 passed.

`Entry` records application/menu/default-detail/panel references. `Ops` is
`create/row`. Runtime checks pair `getlistview + querydata` for list-like
Views and `getreaditemview + initnew` for detail Views without persisting data.

| ID | Name | Kind | Renderer | Entry | Routes | Ops | Runtime |
|---:|---|---|---|---|---|---:|---|
| 100 | OrderList | chart | viewWithChart | app-default,menu,panel:Item,panel:linechart,panel:List,contains:List | /view100 | 0/2 | pass |
| 101 | OrderItemList | list | view | direct | /view101 | 0/0 | pass |
| 103 | OrderSudoku | sudoku | Sudoku | contains:Group,contains:Item,contains:linechart,contains:List,contains:Map | /view103 | 0/0 | pass |
| 104 | OrderSudokuGroup | list | view | panel:Group,contains:Item,contains:List | /view104 | 0/0 | pass |
| 107 | ViewItem列表 | list | view | direct | /view107 | 1/1 | pass |
| 109 | View列表 | list | view | direct | /view109 | 1/1 | pass |
| 111 | AuthorizedUser列表 | list | view | direct | /view111 | 1/1 | pass |
| 113 | User列表 | list | view | direct | /view113 | 1/1 | pass |
| 115 | Department列表 | list | view | direct | /view115 | 1/1 | pass |
| 117 | Company列表 | list | view | direct | /view117 | 1/1 | pass |
| 119 | MenuItem列表 | list | view | direct | /view119 | 1/1 | pass |
| 121 | Role列表 | list | view | direct | /view121 | 1/1 | pass |
| 123 | ApplicationDatabase列表 | list | view | direct | /view123 | 1/1 | pass |
| 125 | DataBaseSource列表 | list | view | direct | /view125 | 1/1 | pass |
| 127 | DbApplication列表 | list | view | direct | /view127 | 1/1 | pass |
| 129 | WorkingDatabase列表 | list | view | direct | /view129 | 1/1 | pass |
| 131 | EventDefinition列表 | list | view | direct | /view131 | 1/1 | pass |
| 133 | EventMessage列表 | list | view | direct | /view133 | 1/1 | pass |
| 135 | EventRecord列表 | list | view | direct | /view135 | 1/1 | pass |
| 137 | EnumValue列表 | list | view | direct | /view137 | 1/1 | pass |
| 139 | MultiDbMap列表 | list | view | direct | /view139 | 1/1 | pass |
| 141 | OperationCommand列表 | list | view | direct | /view141 | 1/1 | pass |
| 143 | PropertyTrigger列表 | list | view | direct | /view143 | 1/1 | pass |
| 145 | Property列表 | list | view | direct | /view145 | 1/1 | pass |
| 147 | Model列表 | list | view | direct | /view147 | 1/1 | pass |
| 149 | OperationParam列表 | list | view | direct | /view149 | 1/1 | pass |
| 151 | Operation列表 | list | view | direct | /view151 | 1/1 | pass |
| 153 | Relation列表 | list | view | direct | /view153 | 1/1 | pass |
| 155 | Trigger列表 | list | view | direct | /view155 | 1/1 | pass |
| 157 | AppInstalledEnumValue列表 | list | view | direct | /view157 | 1/1 | pass |
| 159 | AppInstalledModel列表 | list | view | direct | /view159 | 1/1 | pass |
| 161 | AppInstalledModelTrigger列表 | list | view | direct | /view161 | 1/1 | pass |
| 163 | AppInstalledModelTriggerCommand列表 | list | view | direct | /view163 | 1/1 | pass |
| 165 | AppInstalledModule列表 | list | view | direct | /view165 | 1/1 | pass |
| 167 | AppInstalledMultiDbMap列表 | list | view | direct | /view167 | 1/1 | pass |
| 169 | AppInstalledOperation列表 | list | view | direct | /view169 | 1/1 | pass |
| 171 | AppInstalledOperationCommand列表 | list | view | direct | /view171 | 1/1 | pass |
| 173 | AppInstalledOperationParam列表 | list | view | direct | /view173 | 1/1 | pass |
| 175 | AppInstalledOperationView列表 | list | view | direct | /view175 | 1/1 | pass |
| 177 | AppInstalledOperationViewItem列表 | list | view | direct | /view177 | 1/1 | pass |
| 179 | AppInstalledProperty列表 | list | view | direct | /view179 | 1/1 | pass |
| 181 | AppInstalledPropertyTrigger列表 | list | view | direct | /view181 | 1/1 | pass |
| 183 | AppInstalledPropertyTriggerCommand列表 | list | view | direct | /view183 | 1/1 | pass |
| 185 | AppInstalledRelation列表 | list | view | direct | /view185 | 1/1 | pass |
| 187 | AppInstalledView列表 | list | view | direct | /view187 | 1/1 | pass |
| 189 | AppInstalledViewFile列表 | list | view | direct | /view189 | 1/1 | pass |
| 191 | AppInstalledViewItem列表 | list | view | direct | /view191 | 1/1 | pass |
| 193 | AppInstalledViewOperation列表 | list | view | direct | /view193 | 1/1 | pass |
| 195 | AppSystemView列表 | list | view | direct | /view195 | 1/1 | pass |
| 197 | StoreDatabase列表 | list | view | direct | /view197 | 1/1 | pass |
| 199 | ApplicationDefinition列表 | list | view | direct | /view199 | 1/1 | pass |
| 201 | AuthCompany列表 | list | view | direct | /view201 | 1/1 | pass |
| 203 | AuthDepartment列表 | list | view | direct | /view203 | 1/1 | pass |
| 205 | AuthDepartmentSubDepartmentRelation列表 | list | view | direct | /view205 | 1/1 | pass |
| 207 | AuthMenuItem列表 | list | view | direct | /view207 | 1/1 | pass |
| 209 | AuthMenuSubItemRelation列表 | list | view | direct | /view209 | 1/1 | pass |
| 211 | AuthRole列表 | list | view | direct | /view211 | 1/1 | pass |
| 213 | AuthRoleAuthorizedUserRelation列表 | list | view | direct | /view213 | 1/1 | pass |
| 215 | AuthRoleDepartmentRelation列表 | list | view | direct | /view215 | 1/1 | pass |
| 217 | AuthRoleMenuItemRelation列表 | list | view | direct | /view217 | 1/1 | pass |
| 102 | OrderDetail | detail | detailView/item | detail:3,panel:Item | /view102/:id; /new102; /itemview102 | 0/0 | pass |
| 106 | ViewItem详细 | detail | detailView/item | detail:1 | /view106/:id; /new106; /itemview106 | 0/0 | pass |
| 108 | View详细 | detail | detailView/item | detail:1 | /view108/:id; /new108; /itemview108 | 0/0 | pass |
| 110 | AuthorizedUser详细 | detail | detailView/item | detail:1 | /view110/:id; /new110; /itemview110 | 0/0 | pass |
| 112 | User详细 | detail | detailView/item | detail:1 | /view112/:id; /new112; /itemview112 | 0/0 | pass |
| 114 | Department详细 | detail | detailView/item | detail:1 | /view114/:id; /new114; /itemview114 | 0/0 | pass |
| 116 | Company详细 | detail | detailView/item | detail:1 | /view116/:id; /new116; /itemview116 | 0/0 | pass |
| 118 | MenuItem详细 | detail | detailView/item | detail:1 | /view118/:id; /new118; /itemview118 | 0/0 | pass |
| 120 | Role详细 | detail | detailView/item | detail:1 | /view120/:id; /new120; /itemview120 | 0/0 | pass |
| 122 | ApplicationDatabase详细 | detail | detailView/item | detail:1 | /view122/:id; /new122; /itemview122 | 0/0 | pass |
| 124 | DataBaseSource详细 | detail | detailView/item | detail:1 | /view124/:id; /new124; /itemview124 | 0/0 | pass |
| 126 | DbApplication详细 | detail | detailView/item | detail:1 | /view126/:id; /new126; /itemview126 | 0/0 | pass |
| 128 | WorkingDatabase详细 | detail | detailView/item | detail:1 | /view128/:id; /new128; /itemview128 | 0/0 | pass |
| 130 | EventDefinition详细 | detail | detailView/item | detail:1 | /view130/:id; /new130; /itemview130 | 0/0 | pass |
| 132 | EventMessage详细 | detail | detailView/item | detail:1 | /view132/:id; /new132; /itemview132 | 0/0 | pass |
| 134 | EventRecord详细 | detail | detailView/item | detail:1 | /view134/:id; /new134; /itemview134 | 0/0 | pass |
| 136 | EnumValue详细 | detail | detailView/item | detail:1 | /view136/:id; /new136; /itemview136 | 0/0 | pass |
| 138 | MultiDbMap详细 | detail | detailView/item | detail:1 | /view138/:id; /new138; /itemview138 | 0/0 | pass |
| 140 | OperationCommand详细 | detail | detailView/item | detail:1 | /view140/:id; /new140; /itemview140 | 0/0 | pass |
| 142 | PropertyTrigger详细 | detail | detailView/item | detail:1 | /view142/:id; /new142; /itemview142 | 0/0 | pass |
| 144 | Property详细 | detail | detailView/item | detail:1 | /view144/:id; /new144; /itemview144 | 0/0 | pass |
| 146 | Model详细 | detail | detailView/item | detail:1 | /view146/:id; /new146; /itemview146 | 0/0 | pass |
| 148 | OperationParam详细 | detail | detailView/item | detail:1 | /view148/:id; /new148; /itemview148 | 0/0 | pass |
| 150 | Operation详细 | detail | detailView/item | detail:1 | /view150/:id; /new150; /itemview150 | 0/0 | pass |
| 152 | Relation详细 | detail | detailView/item | detail:1 | /view152/:id; /new152; /itemview152 | 0/0 | pass |
| 154 | Trigger详细 | detail | detailView/item | detail:1 | /view154/:id; /new154; /itemview154 | 0/0 | pass |
| 156 | AppInstalledEnumValue详细 | detail | detailView/item | detail:1 | /view156/:id; /new156; /itemview156 | 0/0 | pass |
| 158 | AppInstalledModel详细 | detail | detailView/item | detail:1 | /view158/:id; /new158; /itemview158 | 0/0 | pass |
| 160 | AppInstalledModelTrigger详细 | detail | detailView/item | detail:1 | /view160/:id; /new160; /itemview160 | 0/0 | pass |
| 162 | AppInstalledModelTriggerCommand详细 | detail | detailView/item | detail:1 | /view162/:id; /new162; /itemview162 | 0/0 | pass |
| 164 | AppInstalledModule详细 | detail | detailView/item | detail:1 | /view164/:id; /new164; /itemview164 | 0/0 | pass |
| 166 | AppInstalledMultiDbMap详细 | detail | detailView/item | detail:1 | /view166/:id; /new166; /itemview166 | 0/0 | pass |
| 168 | AppInstalledOperation详细 | detail | detailView/item | detail:1 | /view168/:id; /new168; /itemview168 | 0/0 | pass |
| 170 | AppInstalledOperationCommand详细 | detail | detailView/item | detail:1 | /view170/:id; /new170; /itemview170 | 0/0 | pass |
| 172 | AppInstalledOperationParam详细 | detail | detailView/item | detail:1 | /view172/:id; /new172; /itemview172 | 0/0 | pass |
| 174 | AppInstalledOperationView详细 | detail | detailView/item | detail:1 | /view174/:id; /new174; /itemview174 | 0/0 | pass |
| 176 | AppInstalledOperationViewItem详细 | detail | detailView/item | detail:1 | /view176/:id; /new176; /itemview176 | 0/0 | pass |
| 178 | AppInstalledProperty详细 | detail | detailView/item | detail:1 | /view178/:id; /new178; /itemview178 | 0/0 | pass |
| 180 | AppInstalledPropertyTrigger详细 | detail | detailView/item | detail:1 | /view180/:id; /new180; /itemview180 | 0/0 | pass |
| 182 | AppInstalledPropertyTriggerCommand详细 | detail | detailView/item | detail:1 | /view182/:id; /new182; /itemview182 | 0/0 | pass |
| 184 | AppInstalledRelation详细 | detail | detailView/item | detail:1 | /view184/:id; /new184; /itemview184 | 0/0 | pass |
| 186 | AppInstalledView详细 | detail | detailView/item | detail:1 | /view186/:id; /new186; /itemview186 | 0/0 | pass |
| 188 | AppInstalledViewFile详细 | detail | detailView/item | detail:1 | /view188/:id; /new188; /itemview188 | 0/0 | pass |
| 190 | AppInstalledViewItem详细 | detail | detailView/item | detail:1 | /view190/:id; /new190; /itemview190 | 0/0 | pass |
| 192 | AppInstalledViewOperation详细 | detail | detailView/item | detail:1 | /view192/:id; /new192; /itemview192 | 0/0 | pass |
| 194 | AppSystemView详细 | detail | detailView/item | detail:1 | /view194/:id; /new194; /itemview194 | 0/0 | pass |
| 196 | StoreDatabase详细 | detail | detailView/item | detail:1 | /view196/:id; /new196; /itemview196 | 0/0 | pass |
| 198 | ApplicationDefinition详细 | detail | detailView/item | detail:1 | /view198/:id; /new198; /itemview198 | 0/0 | pass |
| 200 | AuthCompany详细 | detail | detailView/item | detail:1 | /view200/:id; /new200; /itemview200 | 0/0 | pass |
| 202 | AuthDepartment详细 | detail | detailView/item | detail:1 | /view202/:id; /new202; /itemview202 | 0/0 | pass |
| 204 | AuthDepartmentSubDepartmentRelation详细 | detail | detailView/item | detail:1 | /view204/:id; /new204; /itemview204 | 0/0 | pass |
| 206 | AuthMenuItem详细 | detail | detailView/item | detail:1 | /view206/:id; /new206; /itemview206 | 0/0 | pass |
| 208 | AuthMenuSubItemRelation详细 | detail | detailView/item | detail:1 | /view208/:id; /new208; /itemview208 | 0/0 | pass |
| 210 | AuthRole详细 | detail | detailView/item | detail:1 | /view210/:id; /new210; /itemview210 | 0/0 | pass |
| 212 | AuthRoleAuthorizedUserRelation详细 | detail | detailView/item | detail:1 | /view212/:id; /new212; /itemview212 | 0/0 | pass |
| 214 | AuthRoleDepartmentRelation详细 | detail | detailView/item | detail:1 | /view214/:id; /new214; /itemview214 | 0/0 | pass |
| 216 | AuthRoleMenuItemRelation详细 | detail | detailView/item | detail:1 | /view216/:id; /new216; /itemview216 | 0/0 | pass |
| 105 | CustomerMap | map-panel | includes/Map | panel:Map | parent Sudoku/Group | 0/0 | pass |

## Interaction Contract

- `list`: find, report, paging, and metadata-declared create/row operations.
- `chart`: find, data/chart tabs, paging, and metadata-declared row operations.
- `sudoku`: List/Group/Map/Item/line-chart panels and List refresh/paging.
- `detail`: schema-only item metadata plus read/new/edit/save/child interactions.
- `map-panel`: map content and the old passive timestamp/refresh text.
