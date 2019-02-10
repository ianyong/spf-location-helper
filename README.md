# SPF Location Helper
The Singapore Police Force comprises 7 land divisions; each controlling a number of Neighbourhood Police Centres (NPCs) with clearly defined boundaries. In turn, each NPC may have Neighbourhood Police Posts (NPPs) within its boundaries which it controls. These boundaries are not always intuitive as can be seen in the following examples:
- Jurong East NPC being under the control of Clementi Police Division instead of Jurong Police Division
- Tiong Bahru Plaza despite being under the jurisdiction of Central Police Division's Bukit Merah East NPC, is much closer to Clementi Police Division's Bukit Merah West NPC (442m vs 1.6km)
- Bukit Timah NPP being under the control of Clementi Police Division's Clementi NPC as opposed to Tanglin Police Division's Bukit Timah NPC
- Bukit Panjang North NPP being under the control of Woodlands Police Division's Woodlands West NPC as opposed to Jurong Police Division's Bukit Panjang NPC.

The above examples can be confusing not only to the public, but also to new officers. The aim of this unofficial Android application is to display the internal boundaries used in a concise and effective manner.

**The latest release can be found [here](https://github.com/ianyong/SPFLocationHelper/releases/latest).**
## Features
- Map of Singapore with the NPC boundaries overlaid that can be clicked on for more information
- Search bar to search for locations within Singapore
- Reverse geocoding on long press on the map
- Bottom sheet that displays the selected NPC/ NPP's information based off the above input methods
- Find nearest NPC and/ or NPP function
<p float="left">
  <img src="/media/search_bar.gif" width="300"/>
  <img src="/media/reverse_geocode_nearest.gif" width="300"/>
</p>

## History
The idea behind this application was first conceived back in 2017 when I was on filer duty in Clementi Police Division and erroneously thought that Jurong Island fell under the jurisdiction of Jurong Police Division. Subsequently, I realised that the datasets for both the NPC boundaries and police establishments could be found online. Work on this application started in late 2018.
## Datasets
**No non-publicly available information was used in this application.**  
All datasets used in this app were sourced from [data.gov.sg](https://data.gov.sg) and used in accordance to the [Singapore Open Data License v1.0](https://data.gov.sg/open-data-licence).
- [Singapore Police Force NPC Boundary](https://data.gov.sg/dataset/singapore-police-force-npc-boundary)  
Last official update on December 21, 2017.  
Unofficially edited to include the newly created Woodlands Police Division on November 23, 2018.
- [Singapore Police Force Establishments](https://data.gov.sg/dataset/singapore-police-force-establishments)  
Last official update on November 23, 2018.
## Disclaimer
This application is not endorsed by, directly affiliated with, maintained, authorised, or sponsored by the Singapore Police Force. The SPF crest, logos, and information used in this app are owned by, licensed to, or controlled by the SPF. This application is purely a hobby project.
