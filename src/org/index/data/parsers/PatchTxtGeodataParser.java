package org.index.data.parsers;

import org.index.config.ConfigParser;
import org.index.config.configs.MainConfig;
import org.index.config.parsers.MainConfigParser;
import org.index.data.writers.ConvDatGeodataWriter;
import org.index.enums.GeodataBlockTypes;
import org.index.enums.GeodataCellDirectionFlag;
import org.index.enums.GeodataExtensions;
import org.index.model.GeoMainCell;
import org.index.model.GeoRegion;
import org.index.model.blocks.GeoBlock;
import org.index.model.blocks.GeoBlockComplex;
import org.index.model.blocks.GeoBlockFlat;
import org.index.model.blocks.GeoBlockMultiLevel;
import org.index.model.blocks.RawGeoBlock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatchTxtGeodataParser extends AbstractGeodataParser
{
    private final static Pattern PATTERN = Pattern.compile("\\[(\\d*),(\\d*)\\](\\d*)|\\(([0-9-:]*)\\)");

    //    private final int
    //            []  // geo index
    //            []  // cell number
    //            []  // layer
    //            []  // height && nswe
    //            _blocks;
    private int[][][][] _blocks; // = new int[256 * 256][8 * 8][][];

    public PatchTxtGeodataParser(File pathToGeoFile)
    {
        super(GeodataExtensions.PATCH_TXT, pathToGeoFile);
        // _blocks = new int[256 * 256][8 * 8][][];
    }

    @Override
    protected byte[] readFileAsBytes()
    {
        // fillArrayFromFile();
        return null;
    }

    @Override
    public GeoRegion read()
    {
        final GeoRegion geoRegion = new GeoRegion(getXYcord()[0], getXYcord()[1]);
        fillArrayFromFilePandasStyle(geoRegion);
        readBlocks(geoRegion);
        return geoRegion;
    }

    @Override
    protected void readBlocks(GeoRegion geoRegion)
    {
        for (int blockX = 0; blockX < MainConfig.GEO_REGION_SIZE; blockX++)
        {
            for (int blockY = 0; blockY < MainConfig.GEO_REGION_SIZE; blockY++)
            {
                _pos.set(blockX * blockY);
                if (_blocks.length < _pos.get())
                {
                    continue;
                }
                int[][][] cells = _blocks[_pos.get()];
                final GeoBlock block;
                switch (getBlockType(cells))
                {
                    case FLAT:
                    {
                        block = readFlatData(geoRegion);
                        break;
                    }
                    case COMPLEX:
                    {
                        block = readComplexData(geoRegion);
                        break;
                    }
                    case MULTILEVEL:
                    {
                        block = readMultilevelData(geoRegion);
                        break;
                    }
                    default:
                    {
                        continue;
                    }
                }
                block.setXY(blockX, blockY);
                geoRegion.addBlock(blockX, blockY, block);
            }
        }
    }

    @Override
    protected GeoBlock readFlatData(GeoRegion geoRegion)
    {
        int[] rawCell = _blocks[_pos.get()][0][0];
        final GeoBlockFlat block = new GeoBlockFlat(geoRegion);

        short height = (short) (rawCell[0] | 15);
        short nswe   = (short) rawCell[1];

        final GeoMainCell cell = new GeoMainCell(block, 0, 0, 1, height);
        cell.setHeight(height);
        cell.setNswe  (nswe);

        block.addCell(cell);
        return block;
    }

    @Override
    protected GeoBlock readComplexData(GeoRegion geoRegion)
    {
        int[][][] rawCells = _blocks[_pos.get()];
        final GeoBlockComplex block = new GeoBlockComplex(geoRegion);
        block.extendCells(8, 8);
        for (int cellX = 0; cellX < 8; cellX++)
        {
            for (int cellY = 0; cellY < 8; cellY++)
            {
                block.extendLayers(cellX, cellY, 1);

                int[] rawCell = rawCells[cellX * cellY] == null ? null : rawCells[cellX * cellY][0];
                final short height;
                final short nswe;
                if (rawCell == null || rawCells[cellX * cellY].length == 0)
                {
                    height = ((16_000 & 0xfff0) << 1) & 0xfff0;
                    nswe   = (short) 0;
                }
                else
                {
                    height = (short) ((rawCell[0] * 2) & 65520);
                    nswe   = (short) rawCell[1];
                }
                GeoMainCell cell = new GeoMainCell(block, cellX, cellY, 1, height);

                cell.setHeight(height);
                cell.setNswe(nswe);

                block.addCell(cell);
            }
        }
        return block;
    }

    @Override
    protected GeoBlock readMultilevelData(GeoRegion geoRegion)
    {
        int counter = 0;
        int[][][] rawCells = _blocks[_pos.get()];
        final GeoBlockMultiLevel block = new GeoBlockMultiLevel(geoRegion);
        block.extendCells(8, 8);
        for (int cellX = 0; cellX < 8; cellX++)
        {
            for (int cellY = 0; cellY < 8; cellY++)
            {
                int[][] rawCell = rawCells[cellX * cellY] == null ? null : rawCells[cellX * cellY];
                final int layers = rawCell == null ? 0 : rawCell.length;
                block.extendLayers(cellX, cellY, layers);
                for (int index = 0; index < layers; index++)
                {
                    int[] rawLayer = rawCell[index];
                    short height = (short) ((rawLayer[0] * 2) & 65520);
                    final GeoMainCell cell = new GeoMainCell(block, cellX, cellY, index, height);
                    cell.setHeight(height);
                    cell.setNswe((short) rawLayer[1]);

                    block.addCell(cell);
                }
            }
        }
        block.setConvDatType(counter);
        return block;
    }

    private void fillArrayFromFile()
    {
        _blocks = new int[256 * 256][8 * 8][][];
        try (LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(getPathToGeodataFile()))))
        {
            String line;
            Matcher matcher;
            while ((line = lnr.readLine()) != null)
            {
                if ((line = line.trim()).isEmpty())
                {
                    continue;
                }
                matcher = PATTERN.matcher(line);
                if (!matcher.find())
                {
                    continue;
                }
                int cellX      = Integer.parseInt(matcher.group(1));
                int cellY      = Integer.parseInt(matcher.group(2));
                int layers     = Integer.parseInt(matcher.group(3));
                int blockIndex = getBlockIndex(cellX, cellY);
                int cellIndex  = getCellIndex (cellX, cellY);
                int[][] readedLayers = new int[layers][2];
                for (int index = 0; index < layers; index++)
                {
                    matcher.find();
                    String values = matcher.group();
                    String heightAsString = values.split(":", 2)[0];
                    String nsweAsString   = values.split(":", 2)[1];
                    int height = readedLayers[index][0] = Integer.parseInt(heightAsString.substring(1));
                    int nswe   = readedLayers[index][1] = Integer.parseInt(new StringBuilder(nsweAsString.substring(0, nsweAsString.length() - 1)).reverse().toString(), 2);
                }
                _blocks[blockIndex][cellIndex] = readedLayers;
            }
        }
        catch (Exception e)
        {
            System.err.println("File [" + getPathToGeodataFile().getName() + "] loading failed.");
        }
    }

    private int[][] _pandaCells;
    private int _maxCells = 0;

    private void fillArrayFromFilePandasStyle(GeoRegion geoRegion)
    {
        _blocks = new int[256 * 256][8 * 8][][];
        _pandaCells = new int[256][256];

        RawGeoBlock[][] rawBlocks = new RawGeoBlock[8][2048];
        int currentY = 0;
        try (LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(getPathToGeodataFile()))))
        {
            String line;
            Matcher matcher;
            while ((line = lnr.readLine()) != null)
            {
                if ((line = line.trim()).isEmpty())
                {
                    continue;
                }
                matcher = PATTERN.matcher(line);
                if (!matcher.find())
                {
                    continue;
                }
                int pathCellX      = Integer.parseInt(matcher.group(1));
                int pathCellY      = Integer.parseInt(matcher.group(2));
                int layers     = Integer.parseInt(matcher.group(3));
                int blockIndex = getBlockIndex(pathCellX, pathCellY);
                int cellIndex  = getCellIndex (pathCellX, pathCellY);

                final RawGeoBlock block = new RawGeoBlock();
                block.extendCells(1, 1);
                block.extendLayers(0, 0, layers);
                for (int index = 0; index < layers; index++)
                {
                    matcher.find();
                    String values = matcher.group();
                    String heightAsString = values.split(":", 2)[0];
                    String nsweAsString   = values.split(":", 2)[1];
                    int height = Integer.parseInt(heightAsString.substring(1));
                    int nswe   = Integer.parseInt(new StringBuilder(nsweAsString.substring(0, nsweAsString.length() - 1)).reverse().toString(), 2);

                    final GeoMainCell cell = new GeoMainCell(block, 0, 0, index, (short) height);
                    cell.setHeight(height);
                    cell.setNswe((short) nswe);
                    block.addCell(cell);
                }
                rawBlocks[pathCellX - currentY][pathCellY] = block;

                if (pathCellY == 2047 && pathCellX % 8 == 7)
                {
                    for (int index = 0; index < 256; index++)
                    {
                        int counter = 0;
                        short cellsCount = 0;
                        for (int cellX = 0; cellX < 8; cellX++)
                        {
                            for (int cellY = 0; cellY < 8; cellY++)
                            {
                                RawGeoBlock existedBlock = rawBlocks[cellX][index * 8 + cellY];
                                int layersCount = existedBlock.getCells()[0][0].length;
                                if (layersCount == 0)
                                {
                                    cellsCount = 0;
                                    continue;
                                }
                                cellsCount += (short) layersCount;
                                int[][] readedLayers = new int[layersCount][2];
                                for (int layer = 0; layer < existedBlock.getCells()[0][0].length; layer++)
                                {
                                    readedLayers[layer][0] = existedBlock.getCells()[0][0][layer].getHeight();
                                    readedLayers[layer][1] = existedBlock.getCells()[0][0][layer].getNswe();
                                }
                                _blocks[(currentY / 8) * index][counter++] = readedLayers;
                            }
                            if (cellsCount == 0)
                            {
                                break;
                            }
                        }
                        // geoRegion.addBlock(currentY / 8, index, rawBlock);
                        // _pandaCells[currentY / 8][index] = cellsCount;
                        _maxCells = Math.max(_maxCells, cellsCount);
                    }
                    currentY = pathCellX + 1;
                }


            }
        }
        catch (Exception e)
        {
            System.err.println("File [" + getPathToGeodataFile().getName() + "] loading failed.");
        }
    }

    @Override
    public int[] getXYcord()
    {
        if (_xycords == null)
        {
            if (getPathToGeodataFile() == null || !getPathToGeodataFile().isFile() || !ConfigParser.isDigit(getPathToGeodataFile().getName().split("_")[0]) || !ConfigParser.isDigit(getPathToGeodataFile().getName().split("_")[1].split("\\.")[0]))
            {
                _xycords = new int[2];
            }
            else
            {
                return  _xycords = new int[]
                        {
                                Integer.parseInt(getPathToGeodataFile().getName().split("_")[0]),
                                Integer.parseInt(getPathToGeodataFile().getName().split("_")[1].split("\\.")[0])
                        };
            }
        }
        return _xycords;
    }

    @Override
    public boolean validGeoFile()
    {
        return false;
    }

    public static int getBlockIndex(int x, int y)
    {
        int bx = x / 8;
        int by = y / 8;

        return (bx << 8) + by;
    }

    public static int getCellIndex(int x, int y)
    {
        int bx = x % 8;
        int by = y % 8;

        return (bx << 3) + by;
    }

    public static GeodataBlockTypes getBlockType(int[][][] inputCells)
    {
        // input = [cell count][layers][0 = height && 1 = nswe]
        int countOfLayers = 0;

        for (int[][] cell : inputCells)
        {
            countOfLayers = Math.max(countOfLayers, cell == null ? 0 : cell.length);
        }

        if (countOfLayers > 1)
        {
            return GeodataBlockTypes.MULTILEVEL;
        }
        // [layers][0 = height && 1 = nswe]
        int[][] firstCell = inputCells[0];
        boolean isCellLayersZero = false;
        boolean isCellNotEquals1 = false;
        for (int[][] cell : inputCells)
        {
            isCellLayersZero = isCellLayersZero || cell == null || cell.length == 1;
            isCellNotEquals1 = isCellNotEquals1 || firstCell != cell;
        }

        if (isCellLayersZero || isCellNotEquals1 || (firstCell[0][1]) != GeodataCellDirectionFlag.NSWE_ALL.getMask())
        {
            return GeodataBlockTypes.COMPLEX;
        }

        return GeodataBlockTypes.FLAT;
    }

    public static void main(String[] args)
    {
        MainConfigParser.getInstance().load();
        File pathToHere;
        try
        {
            pathToHere = new File("").getCanonicalFile();
        }
        catch (Exception e)
        {
            pathToHere = null;
        }
        AbstractGeodataParser data;
        data = pathToHere == null
                ? new PatchTxtGeodataParser(new File("work/conv_dat/25_15_path.txt"))
                : new PatchTxtGeodataParser(new File(pathToHere, "work/conv_dat/25_15_Classic_path.txt"));

        data.validGeoFile();
        final GeoRegion region = data.read();

        ConvDatGeodataWriter convDatGeodataWriter = new ConvDatGeodataWriter(region, new File(pathToHere, "work/conv_dat/25_15_e_conv.dat"));
        convDatGeodataWriter.write();
    }
}
