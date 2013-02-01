#!/usr/bin/ruby

# The number of transmitters to place
txSizes = [100];
# The random seeds to use to generate values
seeds = [1234]

# Dimensions of the "transmitter" (inner) rectangle
txDimension = [10, 10]
# Dimensions of the "receiver" (outer) rectangle
rxDimension = [12, 12]

# Transmitter distributions
txDistributions = ['2-holes .65', 'circled 1', 'clustered .8 .1', 'dumbbell 4',
		'uniform', 'plus .1', 'sine 4', 'rectangled 1']

# Algorithm names to use
algorithms = ['recursive']

# With a density of 35
# grid = 350x350 = 122,500 points
# recursive = 35x35 = 1225 points
gridDensity = 35; # The number of points in a "unit" (grid) or the entire area (recursive)

def buildConfig(baseName, numDev, algorithm, seed, txDim, rxDim, gridDensity, txDist)
	string = <<-ENDSTR
<edu.rutgers.winlab.junsim.Config>
\t<beta>0.65</beta>
\t<numTransmitters>#{numDev}</numTransmitters>
\t<numReceivers>3</numReceivers>
\t<radioPower>2.0</radioPower>
\t<radioAlpha>2.68</radioAlpha>
\t<squareWidth>#{txDim[0]}</squareWidth>
\t<squareHeight>#{txDim[1]}</squareHeight>
\t<universeWidth>#{rxDim[0]}</universeWidth>
\t<universeHeight>#{rxDim[1]}</universeHeight>
\t<randomSeed>#{seed}</randomSeed>
\t<numTrials>1</numTrials>
\t<outputFileName>#{baseName}.csv</outputFileName>
\t<numThreads>2</numThreads>
\t<maxRangeMeters>40</maxRangeMeters>
\t<experimentType>#{algorithm}</experimentType>
\t<gridDensity>#{gridDensity}</gridDensity>
\t<randomized>false</randomized>
\t<renderConfig>graphics.xml</renderConfig>
\t<transmittersFile>../transmitters-#{numDev}-#{txDist}.ssv</transmittersFile>
\t<receiversFile>#{baseName}_rcv.ssv</receiversFile>
\t<outputBasePath>#{baseName}</outputBasePath>
\t<transmitterDistribution>#{txDist}</transmitterDistribution>
</edu.rutgers.winlab.junsim.Config>
ENDSTR
end # buildConfig


for numTx in txSizes do
	for alg in algorithms do
		for seed in seeds do
			for distro in txDistributions do
				expName = "#{alg}-t#{numTx}-s#{seed}-d#{distro}";
				configString = buildConfig(expName, numTx, alg, seed, txDimension, rxDimension, gridDensity, distro);
				File.open("#{expName}.xml", 'w') { |file|
					file.write(configString);
					file.close;
				}
				puts "Performing #{expName}"
				`nice -n 10 java -mx32g -jar jun-sim.jar '#{expName}.xml'`
				pid = $?.pid
				success = $?.exitstatus
				if success != 0 
					puts "ERROR: Execution failed for #{expName}"
					exit
				end
			end # transmitter distributions
		end # seeds
	end # algorithms
end # tx sizes


