package org.index.data.parsers;

import org.index.config.ConfigParser;
import org.index.enums.GeodataBlockTypes;
import org.index.enums.GeodataCellDirectionFlag;
import org.index.enums.GeodataExtensions;
import org.index.model.GeoMainCell;
import org.index.model.GeoRegion;
import org.index.model.blocks.*;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatchTxtGeodataParser extends AbstractGeodataParser
{
    private final static Pattern PATTERN = Pattern.compile("\\[(\\d*),(\\d*)\\](\\d*)|\\(([0-9-:]*)\\)");

    public PatchTxtGeodataParser(File pathToGeoFile)
    {
        super(GeodataExtensions.PATCH_TXT, pathToGeoFile);
    }

    @Override
    protected byte[] readFileAsBytes()
    {
        return new byte[0];
    }

    @Override
    public GeoRegion read()
    {
        final GeoRegion geoRegion = new GeoRegion(getXYcord()[0], getXYcord()[1]);
        String[][] values = new String[2048][2048];
        readFile(values);
        final GeoRegion rawRegion = readBlocksToRaw(values);
        normaliseGeoData(geoRegion, rawRegion);
        return geoRegion;
    }

    private void readFile(String[][] array)
    {
        try(LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(getPathToGeodataFile()))))
        {
            String line = null;
            while ((line = lnr.readLine()) != null)
            {
                if ((line = line.trim()).isEmpty() || line.charAt(0) != '[')
                {
                    continue;
                }
                final Matcher matcher = PATTERN.matcher(line);
                if (!matcher.find())
                {
                    continue;
                }
                int lineX = getX(matcher);
                int lineY = getY(matcher);
                array[lineX][lineY] = String.valueOf(line);
            }
        }
        catch (FileNotFoundException e)
        {
            System.err.println("File [" + getPathToGeodataFile().getName() + "] not found.");
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
                                Integer.parseInt(getPathToGeodataFile().getName().split("_")[1].split("_")[0])
                        };
            }
        }
        return _xycords;
    }

    protected GeoRegion readBlocksToRaw(String[][] array)
    {
        final GeoRegion geoRegion = new GeoRegion(getXYcord()[0], getXYcord()[1]);
        for (int blockX = 0; blockX < 256; blockX++)
        {
            for (int blockY = 0; blockY < 256; blockY++)
            {
                GeoBlockRaw rawBlock = new GeoBlockRaw(geoRegion);
                rawBlock.extendCells(8, 8);
                geoRegion.addBlock(blockX, blockY, rawBlock);
                for (int cellX = 0; cellX < 8; cellX++)
                {
                    int index2048X = blockX * 8 + cellX;
                    for (int cellY = 0; cellY < 8; cellY++)
                    {
                        int index2048Y = blockY * 8 + cellY;
                        String pathTxtData = array[index2048X][index2048Y];
                        final Matcher matcher = PATTERN.matcher(pathTxtData);
                        if (!matcher.find())
                        {
                            continue;
                        }
                        int layers = getLayers(matcher);
                        if (layers == 0)
                        {
                            rawBlock.extendLayers(cellX, cellY, 1);
                            GeoMainCell cell = new GeoMainCell(rawBlock, cellX, cellY, 1, (short) GeoMainCell.encodeNsweAndHeightToMask((short) 0, GeodataCellDirectionFlag.NONE.getMask()));
                            rawBlock.addCell(cell);
                            continue;
                        }
                        rawBlock.extendLayers(cellX, cellY, layers);
                        for (int layer = 0; layer < layers; layer++)
                        {
                            String[] heighAndNswe = getHeightAndNSWEValues(matcher);
                            short height = Short.parseShort(heighAndNswe[0]);
                            short nswe = 0;
                            nswe = String.valueOf(heighAndNswe[1]).charAt(3) == '1' ? (short) (nswe | GeodataCellDirectionFlag.FLAG_N.getMask()) : nswe;
                            nswe = String.valueOf(heighAndNswe[1]).charAt(2) == '1' ? (short) (nswe | GeodataCellDirectionFlag.FLAG_S.getMask()) : nswe;
                            nswe = String.valueOf(heighAndNswe[1]).charAt(0) == '1' ? (short) (nswe | GeodataCellDirectionFlag.FLAG_E.getMask()) : nswe;
                            nswe = String.valueOf(heighAndNswe[1]).charAt(1) == '1' ? (short) (nswe | GeodataCellDirectionFlag.FLAG_W.getMask()) : nswe;
                            GeoMainCell cell = new GeoMainCell(rawBlock, cellX, cellY, layer, (short) GeoMainCell.encodeNsweAndHeightToMask(height, nswe));
                            cell.setHeight(height);
                            cell.setNswe(nswe);
                            rawBlock.addCell(cell);
                        }
                    }
                }
            }
        }
        return geoRegion;
    }

    private void normaliseGeoData(GeoRegion newRegion, GeoRegion rawRegion)
    {
        for (int blockX = 0; blockX < 256; blockX++)
        {
            for (int blockY = 0; blockY < 256; blockY++)
            {
                GeoBlockRaw geoBlockRaw = (GeoBlockRaw) rawRegion.getBlocks()[blockX][blockY];
                GeodataBlockTypes geodataBlockType = guessBlockType(geoBlockRaw);
                final GeoBlock block;
                switch (geodataBlockType)
                {
                    case FLAT:
                    {
                        block = readFlatData(rawRegion, blockX, blockY);
                        break;
                    }
                    case COMPLEX:
                    {
                        block = readComplexData(rawRegion, blockX, blockY);
                        break;
                    }
                    case MULTILEVEL:
                    {
                        block = readMultilevelData(rawRegion, blockX, blockY);
                        break;
                    }
                    default:
                    {
                        continue;
                    }
                }
                block.setXY(blockX, blockY);
                newRegion.addBlock(blockX, blockY, block);
            }
        }
        newRegion.setCellCount(rawRegion.getCellCount());
    }

    private GeodataBlockTypes guessBlockType(GeoBlockRaw geoBlockRaw)
    {
        int layerCount = -1;
        for (int cellX = 0; cellX < 8; cellX++)
        {
            for (int cellY = 0; cellY < 8; cellY++)
            {
                layerCount = Math.max(layerCount, geoBlockRaw.getCells()[cellX][cellY].length);
            }
        }
        if (layerCount == 0)
        {
            return GeodataBlockTypes.FLAT;
        }
        else if (layerCount != 1)
        {
            return GeodataBlockTypes.MULTILEVEL;
        }
        int maxHeight = Short.MIN_VALUE;
        int height = geoBlockRaw.getCells()[0][0][0].getHeight();
        for (int cellX = 0; cellX < 8; cellX++)
        {
            for (int cellY = 0; cellY < 8; cellY++)
            {
                GeoMainCell cell = geoBlockRaw.getCells()[cellX][cellY][0];
                if (cell.getNswe() != GeodataCellDirectionFlag.NSWE_ALL.getMask() || Math.abs(cell.getHeight() - height) > 30)
                {
                    return GeodataBlockTypes.COMPLEX;
                }
                if (cell.getHeight() > maxHeight)
                {
                    maxHeight = cell.getHeight();
                }
            }
        }
        geoBlockRaw.getCells()[0][0][0].setHeight(maxHeight);
        return GeodataBlockTypes.FLAT;
    }

    @Override
    protected void readBlocks(GeoRegion geoRegion)
    {

    }

    @Override
    protected GeoBlock readFlatData(GeoRegion geoRegion, int... args)
    {
        if (args == null || args.length < 2)
        {
            return null;
        }
        GeoBlockRaw rawBlock = (GeoBlockRaw) geoRegion.getBlocks()[args[0]][args[1]];
        GeoMainCell rawCell  = rawBlock.getCells()[0][0][0];

        final GeoBlockFlat block = new GeoBlockFlat(geoRegion);

//        final GeoMainCell cell = new GeoMainCell(block, 0, 0, 1, (short) GeoMainCell.encodeNsweAndHeightToMask(rawCell.getHeight(), GeodataCellDirectionFlag.NSWE_ALL.getMask()));
        final GeoMainCell cell = new GeoMainCell(block, 0, 0, 1, (short) (rawCell.getHeight() | GeodataCellDirectionFlag.NSWE_ALL.getMask()));
//        cell.setHeight(rawCell.getHeight());
        cell.setHeight(GeoMainCell.decodeHeight((short) cell.getHeightMask()));
        cell.setNswe(GeodataCellDirectionFlag.NSWE_ALL.getMask());

        block.addCell(cell);
        return block;
    }

    @Override
    protected GeoBlock readComplexData(GeoRegion geoRegion, int... args)
    {
        if (args == null || args.length < 2)
        {
            return null;
        }
        GeoBlockRaw rawBlock = (GeoBlockRaw) geoRegion.getBlocks()[args[0]][args[1]];

        final GeoBlockComplex block = new GeoBlockComplex(geoRegion);
        block.extendCells(8, 8);
        for (int x = 0; x < 8; x++)
        {
            for (int y = 0; y < 8; y++)
            {
                GeoMainCell rawCell  = rawBlock.getCells()[x][y][0];

                block.extendLayers(x, y, 1);

                short height = (short) rawCell.getHeightMask();

                GeoMainCell cell = new GeoMainCell(block, x, y, 1, height);
                cell.setHeight(GeoMainCell.decodeHeight(height));
                cell.setNswe(GeoMainCell.decodeNswe(height));

                block.addCell(cell);
            }
        }
        return block;
    }

    @Override
    protected GeoBlock readMultilevelData(GeoRegion geoRegion, int... args)
    {
        if (args == null || args.length < 2)
        {
            return null;
        }
        GeoBlockRaw rawBlock = (GeoBlockRaw) geoRegion.getBlocks()[args[0]][args[1]];

        int convDatCounter = 0;

        final GeoBlockMultiLevel block = new GeoBlockMultiLevel(geoRegion);
        block.extendCells(8, 8);
        for (int x = 0; x < 8; x++)
        {
            for (int y = 0; y < 8; y++)
            {
                convDatCounter += 2;
                GeoMainCell[] layersOfRawCells  = rawBlock.getCells()[x][y];
                final int layers = layersOfRawCells.length;

                block.extendLayers(x, y, layers);
                for (int index = 0; index < layers; index++)
                {
                    convDatCounter += 2;

                    GeoMainCell rawCell = layersOfRawCells[index];

                    short height = (short) rawCell.getHeightMask();
                    final GeoMainCell cell = new GeoMainCell(block, x, y, index, height);
                    cell.setHeight(GeoMainCell.decodeHeight(height));
                    cell.setNswe(GeoMainCell.decodeNswe(height));

                    block.addCell(cell);
                }
            }
        }
        block.setConvDatType(convDatCounter);
        return block;
    }

    private static int getX(Matcher matcher)
    {
        return Integer.parseInt(matcher.group(1));
    }

    private static int getY(Matcher matcher)
    {
        return Integer.parseInt(matcher.group(2));
    }

    private static int getLayers(Matcher matcher)
    {
        return Integer.parseInt(matcher.group(3));
    }

    private static String[] getHeightAndNSWEValues(Matcher matcher)
    {
        String[] heightAndNSWE = new String[2];
        if (matcher.find())
        {
            String[] match = matcher.group().split(":");
            heightAndNSWE[0] = match[0].substring(1);
            heightAndNSWE[1] = match[1].substring(0, match[1].length() - 1);
        }
        return heightAndNSWE;
    }
}
