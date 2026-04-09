package org.index.utils;

import git.index.dummylogger.LoggerImpl;
import org.index.config.configs.MainConfig;
import org.index.enums.GeodataBlockTypes;
import org.index.enums.GeodataCellDirectionFlag;
import org.index.model.GeoMainCell;
import org.index.model.GeoRegion;
import org.index.model.blocks.*;

/**
 * @author Index
 */
public class OptimizeRegionImpl
{
    private final static LoggerImpl LOGGER = new LoggerImpl(OptimizeRegionImpl.class);

    private final GeoRegion _inputGeoRegion;
    private final GeoRegion _outputGeoRegion;

    public OptimizeRegionImpl(GeoRegion inputGeoRegion)
    {
        _inputGeoRegion = inputGeoRegion;
        _outputGeoRegion = new GeoRegion(inputGeoRegion.getX(), inputGeoRegion.getY());
    }

    public GeoRegion getInputGeoRegion()
    {
        return _inputGeoRegion;
    }

    public GeoRegion getOutputGeoRegion()
    {
        return _outputGeoRegion;
    }

    /**
     * EN: Iterates over all blocks in the input region.
     *     MULTILEVEL blocks are copied as-is.
     *     COMPLEX blocks are checked in two steps: strict internal check (NSWE_ALL + equal heights),
     *     then softer check (HEIGH_DIFF tolerance + 8-neighbor compatibility); converted to FLAT if either passes.
     *     FLAT blocks: when {@link MainConfig#LOG_FLAT_BLOCK_ISSUE_ON_OPTIMIZATION} is true, their 8 neighbors
     *     are validated via {@link #checkIfSurroundingBlockCouldBeFlat}; a warning is logged if incompatible. <br>
     * RU: Перебирает все блоки входного региона.
     *     MULTILEVEL-блоки копируются без изменений.
     *     COMPLEX-блоки проверяются в два этапа: строгая внутренняя проверка (NSWE_ALL + равные высоты),
     *     затем мягкая (допуск HEIGH_DIFF + совместимость 8 соседей); при прохождении любого этапа конвертируются в FLAT.
     *     FLAT-блоки: при {@link MainConfig#LOG_FLAT_BLOCK_ISSUE_ON_OPTIMIZATION} = true их 8 соседей
     *     проверяются через {@link #checkIfSurroundingBlockCouldBeFlat}; при несовместимости выводится предупреждение. <br>
     **/
    public void optimizeRegion()
    {
        for (int x = 0; x < MainConfig.GEO_REGION_SIZE; x++)
        {
            for (int y = 0; y < MainConfig.GEO_REGION_SIZE; y++)
            {
                GeoBlock geoBlock = _inputGeoRegion.getBlocks()[x][y];
                if (geoBlock == null)
                {
                    continue;
                }
                GeodataBlockTypes blockType = geoBlock.getBlockType();
                if (blockType.equals(GeodataBlockTypes.MULTILEVEL))
                {
                    _outputGeoRegion.addBlock(x, y, copyGeoBlock(geoBlock));
                }
                else if (blockType.equals(GeodataBlockTypes.COMPLEX))
                {
                    if (checkIfComplexBlockCouldBeFlat(geoBlock))
                    {
                        _outputGeoRegion.addBlock(x, y, transferComplexIntoFlat(geoBlock));
                    }
                    else if (checkIfSurroundingBlockCouldBeFlat(geoBlock, x, y))
                    {
                        _outputGeoRegion.addBlock(x, y, transferComplexIntoFlat(geoBlock));
                    }
                    else
                    {
                        _outputGeoRegion.addBlock(x, y, copyGeoBlock(geoBlock));
                    }
                }
                else if (blockType.equals(GeodataBlockTypes.FLAT))
                {
                    if (MainConfig.LOG_FLAT_BLOCK_ISSUE_ON_OPTIMIZATION)
                    {
                        if (checkIfSurroundingBlockCouldBeFlat(geoBlock, x, y))
                        {
                            LOGGER.warn(String.format("Flat block at [%d][%d] has height-incompatible surrounding block(s)!", x, y));
                        }
                    }
                    _outputGeoRegion.addBlock(x, y, copyGeoBlock(geoBlock));
                }
            }
        }
    }

    /**
     * EN: Checks whether the internal state of a COMPLEX block allows it to become FLAT.
     *     All 64 cells must have NSWE_ALL and the spread between the maximum and minimum
     *     cell height must be the same
     * RU: Проверяет, позволяет ли внутреннее состояние COMPLEX-блока преобразовать его в FLAT.
     *     Все 64 ячейки должны иметь NSWE_ALL, а разброс между максимальной и минимальной высотой ячеек
     *     должны быть равны
     * ==================================================================
     * EN: @param geoBlock the COMPLEX block to evaluate <br>
     * RU: @param geoBlock COMPLEX-блок для проверки <br>
     * @return <br>
     *         {true}  - EN: all cells are NSWE_ALL and height spread is within the allowed tolerance <br>
     *         {true}  - RU: все ячейки имеют NSWE_ALL и разброс высот укладывается в допустимый порог <br>
     *         {false} - EN: at least one cell is not NSWE_ALL or height spread exceeds the tolerance <br>
     *         {false} - RU: хотя бы одна ячейка не имеет NSWE_ALL или разброс высот превышает порог <br>
     **/
    private boolean checkIfComplexBlockCouldBeFlat(GeoBlock geoBlock)
    {
        boolean isAllNswe = true;
        short maxHeight = Short.MIN_VALUE;
        short minHeight = Short.MAX_VALUE;
        for (int cellX = 0; cellX < 8; cellX++)
        {
            for (int cellY = 0; cellY < 8; cellY++)
            {
                GeoMainCell cell = geoBlock.getCells()[cellX][cellY][0];
                isAllNswe = isAllNswe && cell.getNswe() == GeodataCellDirectionFlag.NSWE_ALL.getMask();
                maxHeight = cell.getHeight() > maxHeight ? cell.getHeight() : maxHeight;
                minHeight = cell.getHeight() < minHeight ? cell.getHeight() : minHeight;
            }
        }
        return isAllNswe && (maxHeight == minHeight);
    }

    /**
     * EN: Checks whether all 8 neighboring blocks are height-compatible with the current COMPLEX block,
     *     using the block's average height ((max + min) / 2) as the reference point.
     *     For each direction the border cells of the neighbor that face the current block are inspected.
     *     If a neighbor is out of region bounds or null, the result of {@link MainConfig#CAN_OPTIMIZE_BLOCK_ON_EDGE}
     *     is used for that neighbor.
     *     Additionally, all border cells collected across every non-cliff neighbor are checked as a group:
     *     the spread between their global maximum and minimum height must not exceed
     *     {@link MainConfig#HEIGHT_DIFF_IN_SURROUNDED_BLOCK}. <br>
     * RU: Проверяет, совместимы ли все 8 соседних блоков по высоте с текущим COMPLEX-блоком,
     *     используя среднее значение высоты блока ((max + min) / 2) как точку отсчёта.
     *     Для каждого направления проверяются граничные ячейки соседа, обращённые к текущему блоку.
     *     Если сосед выходит за границы региона или равен null — для него применяется
     *     значение {@link MainConfig#CAN_OPTIMIZE_BLOCK_ON_EDGE}.
     *     Дополнительно: все граничные ячейки, собранные по всем не-обрывным соседям, проверяются совместно —
     *     разброс между их глобальным максимумом и минимумом не должен превышать
     *     {@link MainConfig#HEIGHT_DIFF_IN_SURROUNDED_BLOCK}. <br>
     * ==================================================================
     * EN: @param geoBlock the COMPLEX block whose neighbors are being validated <br>
     * RU: @param geoBlock COMPLEX-блок, чьи соседи проверяются <br>
     * EN: @param blockX   X-index of the block in the region grid <br>
     * RU: @param blockX   X-индекс блока в сетке региона <br>
     * EN: @param blockY   Y-index of the block in the region grid <br>
     * RU: @param blockY   Y-индекс блока в сетке региона <br>
     * @return <br>
     *         {true}  - EN: all neighbors are height-compatible (or edge optimization is allowed) AND the global height spread of all checked border cells is within {@link MainConfig#HEIGHT_DIFF_IN_SURROUNDED_BLOCK} <br>
     *         {true}  - RU: все соседи совместимы по высоте (или оптимизация на краю разрешена) И глобальный разброс высот всех проверенных граничных ячеек укладывается в {@link MainConfig#HEIGHT_DIFF_IN_SURROUNDED_BLOCK} <br>
     *         {false} - EN: at least one neighbor has a border cell whose height deviates too much, or the global border-cell spread exceeds the limit <br>
     *         {false} - RU: хотя бы у одного соседа есть граничная ячейка с чрезмерным отклонением высоты, либо глобальный разброс граничных ячеек превышает порог <br>
     **/
    private boolean checkIfSurroundingBlockCouldBeFlat(GeoBlock geoBlock, int blockX, int blockY)
    {
        boolean isAllNswe = true;
        short maxHeight = Short.MIN_VALUE;
        short minHeight = Short.MAX_VALUE;
        for (int cellX = 0; cellX < geoBlock.getCells().length; cellX++)
        {
            for (int cellY = 0; cellY < geoBlock.getCells()[cellX].length; cellY++)
            {
                GeoMainCell cell = geoBlock.getCells()[cellX][cellY][0];
                isAllNswe = isAllNswe && cell.getNswe() == GeodataCellDirectionFlag.NSWE_ALL.getMask();
                maxHeight = cell.getHeight() > maxHeight ? cell.getHeight() : maxHeight;
                minHeight = cell.getHeight() < minHeight ? cell.getHeight() : minHeight;
            }
        }

        if (!isAllNswe || (maxHeight - minHeight) > MainConfig.HEIGH_DIFF_FOR_OPTIMIZATION)
        {   // разница высот большая
            return false;
        }

        short currentAvg = (short) ((maxHeight + minHeight) / 2);

        int[] dxArr = {-1,  0,  1,  1,  1,  0, -1, -1};
        int[] dyArr = {-1, -1, -1,  0,  1,  1,  1,  0};

        short surroundMinHeight = Short.MAX_VALUE;
        short surroundMaxHeight = Short.MIN_VALUE;

        for (int i = 0; i < 8; i++)
        {
            int nx = blockX + dxArr[i];
            int ny = blockY + dyArr[i];
            if (!isNeighborCompatible(blockX, blockY, nx, ny, currentAvg, minHeight, maxHeight))
            {
                return false;
            }
            short[] borderHeights = getNeighborBorderHeights(blockX, blockY, nx, ny, minHeight, maxHeight);
            if (borderHeights != null)
            {
                for (short h : borderHeights)
                {
                    if (h > surroundMaxHeight) surroundMaxHeight = h;
                    if (h < surroundMinHeight) surroundMinHeight = h;
                }
            }
        }

        // Перевірка: розкид висот граничних комірок усіх не-обривних сусідів не має перевищувати поріг
        if (surroundMaxHeight != Short.MIN_VALUE && (surroundMaxHeight - surroundMinHeight) > MainConfig.HEIGHT_DIFF_IN_SURROUNDED_BLOCK)
        {
            return false;
        }
        return true;
    }

    /**
     * EN: Collects the border cell heights of the neighbor block at (nx, ny) that face the current block at (blockX, blockY).
     *     Uses the same cell-range logic as {@link #isNeighborCompatible}.
     *     Returns null if the neighbor is out of bounds, null, or qualifies as a cliff/wall
     *     (all border cells uniformly above maxHeight + {@link MainConfig#WALL_HEIGH_FOR_OPTIMIZATION}
     *     or uniformly below minHeight - {@link MainConfig#WALL_HEIGH_FOR_OPTIMIZATION}),
     *     so that cliff neighbors are excluded from the global spread check, consistent with
     *     how they are ignored inside {@link #isNeighborCompatible}. <br>
     * RU: Собирает высоты граничных ячеек соседнего блока (nx, ny), обращённых к текущему блоку (blockX, blockY).
     *     Использует ту же логику диапазонов ячеек, что и {@link #isNeighborCompatible}.
     *     Возвращает null, если сосед выходит за границы, равен null или квалифицируется как обрыв/стена
     *     (все граничные ячейки равномерно выше maxHeight + {@link MainConfig#WALL_HEIGH_FOR_OPTIMIZATION}
     *     или равномерно ниже minHeight - {@link MainConfig#WALL_HEIGH_FOR_OPTIMIZATION}),
     *     чтобы обрывные соседи исключались из глобальной проверки разброса — согласованно с тем,
     *     как они игнорируются внутри {@link #isNeighborCompatible}. <br>
     * ==================================================================
     * EN: @param blockX    X-index of the current block <br>
     * RU: @param blockX    X-индекс текущего блока <br>
     * EN: @param blockY    Y-index of the current block <br>
     * RU: @param blockY    Y-индекс текущего блока <br>
     * EN: @param nx        X-index of the neighbor <br>
     * RU: @param nx        X-индекс соседа <br>
     * EN: @param ny        Y-index of the neighbor <br>
     * RU: @param ny        Y-индекс соседа <br>
     * EN: @param minHeight minimum cell height of the current block (used for cliff detection) <br>
     * RU: @param minHeight минимальная высота ячейки текущего блока (используется для определения обрыва) <br>
     * EN: @param maxHeight maximum cell height of the current block (used for cliff detection) <br>
     * RU: @param maxHeight максимальная высота ячейки текущего блока (используется для определения обрыва) <br>
     * @return <br>
     *         {short[]} - EN: heights of the relevant border cells, or null if neighbor is absent/cliff/wall <br>
     *         {short[]} - RU: высоты соответствующих граничных ячеек, или null если сосед отсутствует/обрыв/стена <br>
     **/
    private short[] getNeighborBorderHeights(int blockX, int blockY, int nx, int ny, short minHeight, short maxHeight)
    {
        if (nx < 0 || nx >= MainConfig.GEO_REGION_SIZE || ny < 0 || ny >= MainConfig.GEO_REGION_SIZE)
        {
            return null;
        }
        GeoBlock neighbor = _inputGeoRegion.getBlocks()[nx][ny];
        if (neighbor == null)
        {
            return null;
        }
        int dx = nx - blockX;
        int dy = ny - blockY;
        int cellXStart = (dx == -1) ? 7 : 0;
        int cellXEnd   = (dx ==  1) ? 0 : 7;
        int cellYStart = (dy == -1) ? 7 : 0;
        int cellYEnd   = (dy ==  1) ? 0 : 7;

        int count = (cellXEnd - cellXStart + 1) * (cellYEnd - cellYStart + 1);
        short[] tempHeights = new short[count];
        int idx = 0;
        byte requiredFlag = getRequiredDirectionFlag(dx, dy);
        boolean allAbove = true;
        boolean allBelow = true;

        for (int cx = cellXStart; cx <= cellXEnd; cx++)
        {
            for (int cy = cellYStart; cy <= cellYEnd; cy++)
            {
                byte cellNswe = getNeighborCellNswe(neighbor, cx, cy);
                if ((cellNswe & requiredFlag) != requiredFlag)
                {   // стіна — ця комірка не пропускає рух до поточного блоку, пропускаємо
                    continue;
                }
                short h = getNeighborCellHeight(neighbor, cx, cy);
                tempHeights[idx++] = h;
                if (h <= maxHeight + MainConfig.WALL_HEIGH_FOR_OPTIMIZATION) allAbove = false;
                if (h >= minHeight - MainConfig.WALL_HEIGH_FOR_OPTIMIZATION) allBelow = false;
            }
        }

        // Cliff/wall або всі комірки є стінами — цей сусід ігнорується
        if (idx == 0 || allAbove || allBelow)
        {
            return null;
        }
        if (idx == tempHeights.length)
        {
            return tempHeights;
        }
        short[] result = new short[idx];
        System.arraycopy(tempHeights, 0, result, 0, idx);
        return result;
    }

    /**
     * EN: Determines whether the neighbor block at grid position (nx, ny) is height-compatible with
     *     the current block. Only the border cells of the neighbor that face the current block are inspected
     *     (full column/row for cardinal directions, single corner cell for diagonals).
     *     Special case: if ALL border cells are uniformly above (maxHeight + HEIGH_DIFF_FOR_OPTIMIZATION)
     *     OR uniformly below (minHeight - HEIGH_DIFF_FOR_OPTIMIZATION), the neighbor is considered a
     *     cliff/wall and is ignored — returns true without further checks.
     *     If the neighbor is out of the region bounds or null, returns {@link MainConfig#CAN_OPTIMIZE_BLOCK_ON_EDGE}. <br>
     * RU: Определяет, совместим ли соседний блок в позиции (nx, ny) с текущим блоком по высоте.
     *     Проверяются только граничные ячейки соседа, обращённые к текущему блоку
     *     (полный столбец/строка для кардинальных направлений, одна угловая ячейка для диагоналей).
     *     Особый случай: если ВСЕ граничные ячейки равномерно выше (maxHeight + HEIGH_DIFF_FOR_OPTIMIZATION)
     *     ИЛИ равномерно ниже (minHeight - HEIGH_DIFF_FOR_OPTIMIZATION), сосед считается обрывом/стеной
     *     и игнорируется — возвращается true без дальнейших проверок.
     *     Если сосед выходит за границы региона или равен null — возвращает {@link MainConfig#CAN_OPTIMIZE_BLOCK_ON_EDGE}. <br>
     * ================================================================== <br>
     * EN: @param blockX    X-index of the current block <br>
     * RU: @param blockX    X-индекс текущего блока <br>
     * EN: @param blockY    Y-index of the current block <br>
     * RU: @param blockY    Y-индекс текущего блока <br>
     * EN: @param nx        X-index of the neighbor to check <br>
     * RU: @param nx        X-индекс проверяемого соседа <br>
     * EN: @param ny        Y-index of the neighbor to check <br>
     * RU: @param ny        Y-индекс проверяемого соседа <br>
     * EN: @param currentAvg average height of the current block: (maxHeight + minHeight) / 2 <br>
     * RU: @param currentAvg среднее значение высоты текущего блока: (maxHeight + minHeight) / 2 <br>
     * EN: @param minHeight minimum cell height of the current block <br>
     * RU: @param minHeight минимальная высота ячейки текущего блока <br>
     * EN: @param maxHeight maximum cell height of the current block <br>
     * RU: @param maxHeight максимальная высота ячейки текущего блока <br>
     * @return <br>
     *         {true}  - EN: neighbor is compatible, is a cliff/wall (all cells uniformly far above or below), or edge optimization is allowed <br>
     *         {true}  - RU: сосед совместим, является обрывом/стеной (все ячейки равномерно далеко выше или ниже), или оптимизация на краю разрешена <br>
     *         {false} - EN: at least one border cell height deviates more than HEIGH_DIFF_FOR_OPTIMIZATION from currentAvg <br>
     *         {false} - RU: хотя бы одна граничная ячейка отклоняется от currentAvg более чем на HEIGH_DIFF_FOR_OPTIMIZATION <br>
     **/
    private boolean isNeighborCompatible(int blockX, int blockY, int nx, int ny, short currentAvg, short minHeight, short maxHeight)
    {
        if (nx < 0 || nx >= MainConfig.GEO_REGION_SIZE || ny < 0 || ny >= MainConfig.GEO_REGION_SIZE)
        {
            return MainConfig.CAN_OPTIMIZE_BLOCK_ON_EDGE;
        }
        GeoBlock neighbor = _inputGeoRegion.getBlocks()[nx][ny];
        if (neighbor == null)
        {
            return MainConfig.CAN_OPTIMIZE_BLOCK_ON_EDGE;
        }
        int dx = nx - blockX;
        int dy = ny - blockY;
        int cellXStart = (dx == -1) ? 7 : 0;
        int cellXEnd   = (dx ==  1) ? 0 : 7;
        int cellYStart = (dy == -1) ? 7 : 0;
        int cellYEnd   = (dy ==  1) ? 0 : 7;
        // For diagonal neighbors only the single corner cell is checked (both ranges collapse to one value)
        byte requiredFlag = getRequiredDirectionFlag(dx, dy);
        boolean allWalled = true;
        boolean allAbove = true;
        boolean allBelow = true;
        boolean hasIncompatible = false;
        for (int cx = cellXStart; cx <= cellXEnd; cx++)
        {
            for (int cy = cellYStart; cy <= cellYEnd; cy++)
            {
                byte cellNswe = getNeighborCellNswe(neighbor, cx, cy);
                if ((cellNswe & requiredFlag) != requiredFlag)
                {   // стіна — ця комірка не пропускає рух до поточного блоку
                    continue;
                }
                allWalled = false;
                short neighborHeight = getNeighborCellHeight(neighbor, cx, cy);
                if (neighborHeight <= maxHeight + MainConfig.WALL_HEIGH_FOR_OPTIMIZATION)
                {
                    allAbove = false;
                }
                if (neighborHeight >= minHeight - MainConfig.WALL_HEIGH_FOR_OPTIMIZATION)
                {
                    allBelow = false;
                }
                int diff = neighborHeight - currentAvg;
                if (diff < 0)
                {
                    diff = -diff;
                }
                if (diff > MainConfig.HEIGHT_DIFF_IN_SURROUNDED_BLOCK)
                {
                    hasIncompatible = true;
                }
            }
        }
        // Усі граничні комірки є стінами — сусід повністю заблокований, вважаємо сумісним
        if (allWalled)
        {
            return true;
        }
        // All border cells are uniformly far above or below — treat as cliff/wall, ignore this neighbor
        if (allAbove || allBelow)
        {
            return true;
        }
        return !hasIncompatible;
    }

    /**
     * EN: Returns the height of the cell at the given (cellX, cellY) position within a block.
     *     For FLAT blocks the single cell at [0][0][0] is always returned regardless of coordinates,
     *     because a flat block has only one shared height value.
     *     For COMPLEX and MULTILEVEL blocks layer 0 of the requested cell is used. <br>
     * RU: Возвращает высоту ячейки по заданной позиции (cellX, cellY) внутри блока.
     *     Для FLAT-блоков всегда возвращается единственная ячейка [0][0][0] независимо от координат,
     *     так как плоский блок имеет только одно общее значение высоты.
     *     Для COMPLEX и MULTILEVEL блоков используется слой 0 запрошенной ячейки. <br>
     * ==================================================================
     * EN: @param block  the block to read from <br>
     * RU: @param block  блок, из которого считывается высота <br>
     * EN: @param cellX  X-index of the cell within the block (0–7) <br>
     * RU: @param cellX  X-индекс ячейки внутри блока (0–7) <br>
     * EN: @param cellY  Y-index of the cell within the block (0–7) <br>
     * RU: @param cellY  Y-индекс ячейки внутри блока (0–7) <br>
     * @return <br>
     *         {short} - EN: height value of the selected cell <br>
     *         {short} - RU: значение высоты выбранной ячейки <br>
     **/
    private short getNeighborCellHeight(GeoBlock block, int cellX, int cellY)
    {
        if (GeodataBlockTypes.FLAT.equals(block.getBlockType()))
        {
            return block.getCells()[0][0][0].getHeight();
        }
        return block.getCells()[cellX][cellY][0].getHeight();
    }

    /**
     * EN: Returns the NSWE flags of the cell at the given (cellX, cellY) position within a block.
     *     For FLAT blocks the single cell at [0][0][0] is always used regardless of coordinates.
     *     For COMPLEX and MULTILEVEL blocks layer 0 of the requested cell is used. <br>
     * RU: Возвращает NSWE-флаги ячейки по заданной позиции (cellX, cellY) внутри блока.
     *     Для FLAT-блоков всегда используется единственная ячейка [0][0][0] независимо от координат.
     *     Для COMPLEX и MULTILEVEL блоков используется слой 0 запрошенной ячейки. <br>
     * ==================================================================
     * EN: @param block  the block to read from <br>
     * RU: @param block  блок, из которого считывается NSWE <br>
     * EN: @param cellX  X-index of the cell within the block (0–7) <br>
     * RU: @param cellX  X-индекс ячейки внутри блока (0–7) <br>
     * EN: @param cellY  Y-index of the cell within the block (0–7) <br>
     * RU: @param cellY  Y-индекс ячейки внутри блока (0–7) <br>
     * @return <br>
     *         {byte} - EN: NSWE mask of the selected cell <br>
     *         {byte} - RU: NSWE-маска выбранной ячейки <br>
     **/
    private byte getNeighborCellNswe(GeoBlock block, int cellX, int cellY)
    {
        if (GeodataBlockTypes.FLAT.equals(block.getBlockType()))
        {
            return block.getCells()[0][0][0].getNswe();
        }
        return block.getCells()[cellX][cellY][0].getNswe();
    }

    /**
     * EN: Returns the combined NSWE flag mask that a border cell of a neighbor must have
     *     to allow movement toward the current block.
     *     Cardinal neighbors require a single directional flag; diagonal neighbors require
     *     both cardinal components (bitwise OR), because diagonal movement needs passage in each axis. <br>
     * RU: Возвращает комбинированную маску NSWE-флагов, которую должна иметь граничная ячейка соседа,
     *     чтобы допускать движение к текущему блоку.
     *     Кардинальным соседям требуется один флаг направления; диагональным — оба кардинальных компонента
     *     (побитовое ИЛИ), так как диагональное движение требует прохода по каждой оси. <br>
     * ==================================================================
     * EN: @param dx  delta X from current block to neighbor (-1, 0, +1) <br>
     * RU: @param dx  дельта X от текущего блока к соседу (-1, 0, +1) <br>
     * EN: @param dy  delta Y from current block to neighbor (-1, 0, +1) <br>
     * RU: @param dy  дельта Y от текущего блока к соседу (-1, 0, +1) <br>
     * @return <br>
     *         {byte} - EN: bitmask of required NSWE flags the neighbor cell must satisfy <br>
     *         {byte} - RU: битовая маска требуемых NSWE-флагов, которым должна удовлетворять ячейка соседа <br>
     **/
    private byte getRequiredDirectionFlag(int dx, int dy)
    {
        byte flag = 0;
        // Neighbor is to the left  → must allow East  movement toward current block
        // Neighbor is to the right → must allow West  movement toward current block
        // Neighbor is above        → must allow South movement toward current block
        // Neighbor is below        → must allow North movement toward current block
        if (dx == -1) flag |= GeodataCellDirectionFlag.FLAG_E.getMask();
        if (dx ==  1) flag |= GeodataCellDirectionFlag.FLAG_W.getMask();
        if (dy == -1) flag |= GeodataCellDirectionFlag.FLAG_S.getMask();
        if (dy ==  1) flag |= GeodataCellDirectionFlag.FLAG_N.getMask();
        return flag;
    }

    private GeoBlock transferComplexIntoFlat(GeoBlock geoBlock)
    {
        short maxHeight = Short.MIN_VALUE;
        short minHeight = Short.MAX_VALUE;

        for (int cellX = 0; cellX < 8; cellX++)
        {
            for (int cellY = 0; cellY < 8; cellY++)
            {
                GeoMainCell cell = geoBlock.getCells()[cellX][cellY][0];
                maxHeight = cell.getHeight() > maxHeight ? cell.getHeight() : maxHeight;
                minHeight = cell.getHeight() < minHeight ? cell.getHeight() : minHeight;
            }
        }

        final GeoBlockFlat block = new GeoBlockFlat(_outputGeoRegion);

        final GeoMainCell cell = new GeoMainCell(block, 0, 0, 1);
        cell.setHeight(maxHeight);
        cell.setMinHeight(minHeight);
        cell.setNswe(GeodataCellDirectionFlag.NSWE_ALL.getMask());

        block.addCell(cell);
        return block;
    }

    private GeoBlock copyGeoBlock(GeoBlock geoBlock)
    {
        GeoBlock outputGeoBlock = null;
        if (geoBlock.getBlockType() == GeodataBlockTypes.FLAT)
        {
            outputGeoBlock = new GeoBlockFlat(_outputGeoRegion);
        }
        if (geoBlock.getBlockType() == GeodataBlockTypes.COMPLEX)
        {
            outputGeoBlock = new GeoBlockComplex(_outputGeoRegion);
            outputGeoBlock.extendCells(8, 8);
        }
        if (geoBlock.getBlockType() == GeodataBlockTypes.MULTILEVEL)
        {
            outputGeoBlock = new GeoBlockMultiLevel(_outputGeoRegion);
            outputGeoBlock.extendCells(8, 8);
        }
        if (outputGeoBlock == null)
        {   // не думаю... ну только если сюда Raw блок засунут
            return null;
        }
        for (int xCell = 0; xCell < geoBlock.getCells().length; xCell++)
        {
            for (int yCell = 0; yCell < geoBlock.getCells()[xCell].length; yCell++)
            {
                int layerCount = geoBlock.getCells()[xCell][yCell].length;
                outputGeoBlock.extendLayers(xCell, yCell, layerCount);

                for (int layer = 0; layer < layerCount; layer++)
                {
                    GeoMainCell curCell = geoBlock.getCells()[xCell][yCell][layer];
                    GeoMainCell newCell = new GeoMainCell(outputGeoBlock, xCell, yCell, layer);

                    newCell.setHeight(curCell.getHeight());
                    newCell.setMinHeight(curCell.getMinHeight());
                    newCell.setNswe(curCell.getNswe());

                    outputGeoBlock.addCell(newCell);
                }
            }
        }
        outputGeoBlock.setConvDatType(geoBlock.getConvDatType());
        return outputGeoBlock;
    }
}
