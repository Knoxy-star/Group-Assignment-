// Central registry describing each hazard's own 5 indicators (from the
// brief) plus which backend service owns it. Everything else in the app
// (forms, tables, routing) is generic and driven off this file, instead
// of duplicating near-identical pages 5 times the way the old Thymeleaf
// version did. Field `key`s match each service's IncidentCreateRequest /
// IncidentUpdateRequest / IncidentResponse JSON exactly.

export const HAZARDS = [
  {
    slug: 'flood',
    label: 'Flood',
    hazardEnum: 'FLOOD',
    servicePath: 'flood-service',
    fields: [
      { key: 'peakWaterLevelMetres', label: 'Peak water level (m)', type: 'number', step: '0.1' },
      {
        key: 'catchment',
        label: 'Catchment',
        type: 'select',
        options: ['GWAYI', 'MANYAME', 'MAZOWE', 'MZINGWANE', 'RUNDE', 'SANYATI', 'SAVE'],
      },
      { key: 'householdsDisplaced', label: 'Households displaced', type: 'number', integer: true },
      { key: 'areaFloodedHectares', label: 'Area flooded (ha)', type: 'number', step: '0.1' },
      { key: 'inundationDurationDays', label: 'Inundation duration (days)', type: 'number', integer: true },
    ],
  },
  {
    slug: 'drought',
    label: 'Drought',
    hazardEnum: 'DROUGHT',
    servicePath: 'drought-service',
    fields: [
      { key: 'rainfallDeficitMm', label: 'Rainfall deficit (mm)', type: 'number', step: '0.1' },
      { key: 'consecutiveDryDays', label: 'Consecutive dry days', type: 'number', integer: true },
      { key: 'cropFailurePercent', label: 'Crop failure (%)', type: 'number', step: '0.1' },
      { key: 'peopleFacingWaterShortage', label: 'People facing water shortage', type: 'number', integer: true },
      { key: 'livestockMortalityCount', label: 'Livestock mortality count', type: 'number', integer: true },
    ],
  },
  {
    slug: 'fire',
    label: 'Fire',
    hazardEnum: 'FIRE',
    servicePath: 'fire-service',
    fields: [
      { key: 'areaBurnedHectares', label: 'Area burned (ha)', type: 'number', step: '0.1' },
      { key: 'suspectedCause', label: 'Suspected cause', type: 'select', options: ['NATURAL', 'ACCIDENTAL', 'DELIBERATE'] },
      { key: 'injuriesFatalitiesCount', label: 'Injuries / fatalities', type: 'number', integer: true },
      { key: 'structuresDestroyedCount', label: 'Structures destroyed', type: 'number', integer: true },
      { key: 'contained', label: 'Contained?', type: 'boolean', trueLabel: 'Contained', falseLabel: 'Still burning' },
    ],
  },
  {
    slug: 'zoonotic-disease',
    label: 'Zoonotic disease',
    hazardEnum: 'ZOONOTIC_DISEASE',
    servicePath: 'zoonotic-disease-service',
    fields: [
      { key: 'diseaseName', label: 'Disease / pathogen name', type: 'text' },
      {
        key: 'animalSpecies',
        label: 'Animal species',
        type: 'select',
        options: ['CATTLE', 'GOATS', 'SHEEP', 'PIGS', 'POULTRY', 'DOGS', 'WILDLIFE', 'OTHER'],
      },
      { key: 'confirmedAnimalCases', label: 'Confirmed animal cases', type: 'number', integer: true },
      { key: 'outbreakClassification', label: 'Classification', type: 'select', options: ['CLUSTER', 'OUTBREAK'] },
      { key: 'humanCasesCount', label: 'Confirmed human cases', type: 'number', integer: true },
    ],
  },
  {
    slug: 'mining-accident',
    label: 'Mining accident',
    hazardEnum: 'MINING_ACCIDENT',
    servicePath: 'mining-accident-service',
    fields: [
      { key: 'mineName', label: 'Mine name', type: 'text' },
      { key: 'mineType', label: 'Mine type', type: 'select', options: ['FORMAL', 'ARTISANAL'] },
      {
        key: 'accidentType',
        label: 'Accident type',
        type: 'select',
        options: ['COLLAPSE', 'GAS_EXPLOSION', 'FLOODING', 'FALL_OF_GROUND'],
      },
      { key: 'trappedOrInjuredCount', label: 'Trapped / injured', type: 'number', integer: true },
      { key: 'fatalitiesCount', label: 'Fatalities', type: 'number', integer: true },
      { key: 'rescueOngoing', label: 'Rescue ongoing?', type: 'boolean', trueLabel: 'Ongoing', falseLabel: 'Not ongoing' },
    ],
  },
];

export function hazardBySlug(slug) {
  return HAZARDS.find((h) => h.slug === slug);
}

export function hazardByEnum(hazardEnum) {
  return HAZARDS.find((h) => h.hazardEnum === hazardEnum);
}

// Recorders/supervisors have exactly one hazard (from their JWT). This
// maps that enum back to the route slug they're allowed to work in.
export function slugForHazardEnum(hazardEnum) {
  return hazardByEnum(hazardEnum)?.slug;
}

export const SEVERITIES = ['LOW', 'MODERATE', 'HIGH', 'CRITICAL'];
export const STATUSES = ['PENDING', 'APPROVED', 'REJECTED', 'CORRECTIONS_REQUESTED'];
